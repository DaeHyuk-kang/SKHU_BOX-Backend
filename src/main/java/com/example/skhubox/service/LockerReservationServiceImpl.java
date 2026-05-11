package com.example.skhubox.service;

import com.example.skhubox.common.RedisKeys;
import com.example.skhubox.domain.locker.Locker;
import com.example.skhubox.domain.operation.OperationLogType;
import com.example.skhubox.domain.reservation.LockerReservation;
import com.example.skhubox.domain.reservation.ReservationStatus;
import com.example.skhubox.domain.user.User;
import com.example.skhubox.dto.LockerReservationResponse;
import com.example.skhubox.dto.LockerResponse;
import com.example.skhubox.dto.QueueResponse;
import com.example.skhubox.dto.ReservationHistoryResponse;
import com.example.skhubox.exception.BusinessException;
import com.example.skhubox.exception.ErrorCode;
import com.example.skhubox.repository.LockerRepository;
import com.example.skhubox.repository.UserRepository;
import com.example.skhubox.repository.LockerReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LockerReservationServiceImpl implements LockerReservationService {

    private final UserRepository userRepository;
    private final LockerRepository lockerRepository;
    private final LockerReservationRepository lockerReservationRepository;
    private final QueueModeSettingService queueModeSettingService;
    private final WaitingQueueService waitingQueueService;
    private final NotificationService notificationService;
    private final ReservationExpirationService reservationExpirationService;
    private final OperationLogService operationLogService;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public LockerReservationResponse reserveLocker(String studentNumber, Long lockerId) {
        reservationExpirationService.expireOverdueReservations();

        String lockKey = RedisKeys.LOCKER_LOCK + lockerId;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", 5, TimeUnit.SECONDS);
        
        if (Boolean.FALSE.equals(acquired)) {
            log.warn("[Concurrency] Lock acquisition failed for locker {} by user {}", lockerId, studentNumber);
            throw new BusinessException(ErrorCode.LOCK_ACQUISITION_FAILED);
        }

        try {
            if (queueModeSettingService.isQueueModeEnabled()) {
                Long myRank = waitingQueueService.getRank(studentNumber, lockerId);

                if (myRank == null) {
                    throw new BusinessException(ErrorCode.QUEUE_MODE_RESERVATION_BLOCKED,
                            "현재 대기열 모드입니다. 먼저 대기열에 등록해주세요.");
                }

                if (!waitingQueueService.isFirstUser(studentNumber, lockerId)) {
                    log.warn("[Queue] User {} is in queue but NOT first for locker {}. Current Rank: {}",
                            studentNumber, lockerId, myRank);
                    throw new BusinessException(ErrorCode.QUEUE_MODE_RESERVATION_BLOCKED,
                            "아직 본인 차례가 아닙니다. 내 순번: " + myRank + "번");
                }

                log.info("[Queue] User {} is FIRST in queue for locker {}. Proceeding to reservation.", studentNumber, lockerId);
            }

            User user = getUser(studentNumber);
            Locker locker = getLockedLocker(lockerId);

            validateReservable(user, locker);

            try {
                LockerReservation reservation = new LockerReservation(user, locker);
                LockerReservation savedReservation = lockerReservationRepository.saveAndFlush(reservation);

                // 사물함 자체 상태 업데이트
                locker.occupy(savedReservation.getExpiredAt());

                if (queueModeSettingService.isQueueModeEnabled()) {
                    waitingQueueService.removeFromQueue(studentNumber, lockerId);
                }

                // 알림 생성
                notificationService.createNotification(
                        user,
                        "사물함 예약 완료",
                        String.format("[%s] %s번 사물함 예약이 완료되었습니다. 만료일: %s", 
                                locker.getBuilding(), locker.getLockerNumber(), savedReservation.getExpiredAt().toLocalDate()),
                        com.example.skhubox.domain.notification.NotificationType.RESERVATION
                );
                operationLogService.log(
                        OperationLogType.RESERVATION_ASSIGNED,
                        "사물함 예약 완료",
                        String.format("%s 사용자가 %s번 사물함을 예약했습니다.", studentNumber, locker.getLockerNumber())
                );

                log.info("[Reservation-Success] User {} successfully reserved locker {}.", studentNumber, lockerId);
                return toResponse(savedReservation, "사물함 예약이 완료되었습니다.");
            } catch (DataIntegrityViolationException e) {
                log.error("[Reservation-Failed] Concurrent DB reservation for locker {}: {}", lockerId, e.getMessage());
                throw new BusinessException(ErrorCode.ALREADY_RESERVED_LOCKER);
            }
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    public LockerReservationResponse returnLocker(String studentNumber) {
        reservationExpirationService.expireOverdueReservations();

        User user = getUser(studentNumber);

        LockerReservation reservation = lockerReservationRepository
                .findByUser_IdAndStatusAndExpiredAtAfter(user.getId(), ReservationStatus.ACTIVE, LocalDateTime.now())
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_ACTIVE_RESERVATION));

        Long lockerId = reservation.getLocker().getId();
        Locker locker = getLockedLocker(lockerId);

        reservation.returnReservation();
        locker.release();
        operationLogService.log(
                OperationLogType.RESERVATION_RETURNED,
                "사물함 반납 완료",
                String.format("%s 사용자가 %s번 사물함을 반납했습니다.", studentNumber, locker.getLockerNumber())
        );

        log.info("[Return-Success] User {} returned locker {}.", studentNumber, lockerId);
        return toResponse(reservation, "사물함 반납이 완료되었습니다.");
    }

    @Override
    public LockerReservationResponse changeLocker(String studentNumber, Long newLockerId) {
        reservationExpirationService.expireOverdueReservations();

        User user = getUser(studentNumber);

        LockerReservation currentReservation = lockerReservationRepository
                .findByUser_IdAndStatusAndExpiredAtAfter(user.getId(), ReservationStatus.ACTIVE, LocalDateTime.now())
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_ACTIVE_RESERVATION));

        Long currentLockerId = currentReservation.getLocker().getId();

        if (currentLockerId.equals(newLockerId)) {
            throw new BusinessException(ErrorCode.SAME_LOCKER_CHANGE);
        }

        Long firstId = Math.min(currentLockerId, newLockerId);
        Long secondId = Math.max(currentLockerId, newLockerId);

        Locker firstLocker = getLockedLocker(firstId);
        Locker secondLocker = getLockedLocker(secondId);

        Locker currentLocker = firstId.equals(currentLockerId) ? firstLocker : secondLocker;
        Locker newLocker = firstId.equals(newLockerId) ? firstLocker : secondLocker;

        validateNewLocker(newLocker);

        try {
            currentReservation.returnReservation();
            currentLocker.release();

            LockerReservation newReservation = new LockerReservation(user, newLocker);
            LockerReservation savedReservation = lockerReservationRepository.saveAndFlush(newReservation);
            newLocker.occupy(savedReservation.getExpiredAt());
            operationLogService.log(
                    OperationLogType.RESERVATION_CHANGED,
                    "사물함 변경 완료",
                    String.format("%s 사용자가 %s번에서 %s번 사물함으로 변경했습니다.",
                            studentNumber, currentLocker.getLockerNumber(), newLocker.getLockerNumber())
            );
            
            log.info("[Change-Success] User {} changed locker from {} to {}. New Expiry: {}", 
                    studentNumber, currentLockerId, newLockerId, savedReservation.getExpiredAt());
            return toResponse(savedReservation, "사물함 변경이 완료되었습니다.");
        } catch (DataIntegrityViolationException e) {
            log.error("[Change-Failed] Concurrent DB change to locker {}: {}", newLockerId, e.getMessage());
            throw new BusinessException(ErrorCode.ALREADY_RESERVED_LOCKER);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<LockerResponse> getAllLockers() {
        return lockerRepository.findAll().stream()
                .map(LockerResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LockerReservationResponse getMyReservation(String studentNumber) {
        reservationExpirationService.expireOverdueReservations();

        User user = getUser(studentNumber);

        LockerReservation reservation = lockerReservationRepository
                .findByUser_IdAndStatusAndExpiredAtAfter(user.getId(), ReservationStatus.ACTIVE, LocalDateTime.now())
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_ACTIVE_RESERVATION));

        return toResponse(reservation, "현재 예약 정보 조회 성공");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationHistoryResponse> getMyReservationHistory(String studentNumber) {
        User user = getUser(studentNumber);
        return lockerReservationRepository.findAllByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(ReservationHistoryResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public void updateExpiryDate(Long reservationId, LocalDateTime newExpiryDate) {
        LockerReservation reservation = lockerReservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        
        reservation.updateExpiryDate(newExpiryDate);
        reservation.getLocker().occupy(newExpiryDate); // 사물함 테이블의 만료일도 동기화
        log.info("[Admin] Updated expiry date for reservation {} and locker {} to {}", 
                reservationId, reservation.getLocker().getId(), newExpiryDate);
    }

    @Override
    @Transactional
    public void updateAllActiveExpirations(LocalDateTime newExpiryDate) {
        List<LockerReservation> activeReservations = lockerReservationRepository.findAllByStatus(ReservationStatus.ACTIVE);
        
        for (LockerReservation reservation : activeReservations) {
            reservation.updateExpiryDate(newExpiryDate);
            reservation.getLocker().occupy(newExpiryDate);
        }
        
        log.info("[Admin] Bulk updated {} active reservations to expiry date: {}", activeReservations.size(), newExpiryDate);
    }

    // --- Private Helper Methods ---

    private User getUser(String studentNumber) {
        return userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Locker getLockedLocker(Long lockerId) {
        return lockerRepository.findByIdWithPessimisticLock(lockerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_NOT_FOUND));
    }

    private void validateReservable(User user, Locker locker) {
        if (!locker.isNormal()) {
            throw new BusinessException(ErrorCode.LOCKER_NOT_NORMAL);
        }

        if (lockerReservationRepository.existsByUser_IdAndStatusAndExpiredAtAfter(
                user.getId(), ReservationStatus.ACTIVE, LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_HAS_LOCKER);
        }

        if (lockerReservationRepository.existsByLocker_IdAndStatusAndExpiredAtAfter(
                locker.getId(), ReservationStatus.ACTIVE, LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.ALREADY_RESERVED_LOCKER);
        }
    }

    private void validateNewLocker(Locker newLocker) {
        if (!newLocker.isNormal()) {
            throw new BusinessException(ErrorCode.LOCKER_NOT_NORMAL);
        }

        if (lockerReservationRepository.existsByLocker_IdAndStatusAndExpiredAtAfter(
                newLocker.getId(), ReservationStatus.ACTIVE, LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.ALREADY_RESERVED_LOCKER);
        }
    }

    private LockerReservationResponse toResponse(LockerReservation reservation, String message) {
        return new LockerReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getLocker().getId(),
                reservation.getStatus().name(),
                reservation.getExpiredAt() != null ? reservation.getExpiredAt().toString() : null,
                message
        );
    }
}
