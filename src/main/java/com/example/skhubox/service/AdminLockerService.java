package com.example.skhubox.service;

import com.example.skhubox.domain.locker.Locker;
import com.example.skhubox.domain.locker.LockerStatus;
import com.example.skhubox.domain.reservation.LockerReservation;
import com.example.skhubox.domain.reservation.ReservationStatus;
import com.example.skhubox.domain.user.User;
import com.example.skhubox.dto.admin.AdminLockerAddRequest;
import com.example.skhubox.dto.admin.AdminLockerResponse;
import com.example.skhubox.exception.BusinessException;
import com.example.skhubox.exception.ErrorCode;
import com.example.skhubox.repository.LockerRepository;
import com.example.skhubox.repository.LockerReservationRepository;
import com.example.skhubox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminLockerService {

    private final LockerRepository lockerRepository;
    private final LockerReservationRepository lockerReservationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AdminLockerResponse> getLockers(String building, Integer floor, String search) {
        List<Locker> lockers;
        if (building != null && !building.isBlank() && floor != null) {
            lockers = lockerRepository.findAllByBuildingAndFloorOrderByLockerNumberAsc(building, floor);
        } else if (building != null && !building.isBlank()) {
            lockers = lockerRepository.findAllByBuildingOrderByFloorAscLockerNumberAsc(building);
        } else {
            lockers = lockerRepository.findAllByOrderByBuildingAscFloorAscLockerNumberAsc();
        }

        // 활성 예약 map (lockerId -> reservation)
        Map<Long, LockerReservation> reservationMap = lockerReservationRepository
                .findAllByStatus(ReservationStatus.ACTIVE)
                .stream()
                .collect(Collectors.toMap(r -> r.getLocker().getId(), r -> r));

        return lockers.stream()
                .map(locker -> AdminLockerResponse.of(locker, reservationMap.get(locker.getId())))
                .filter(r -> matchesSearch(r, search))
                .collect(Collectors.toList());
    }

    public AdminLockerResponse addLocker(AdminLockerAddRequest request) {
        if (lockerRepository.findByLockerNumber(request.getLockerNumber()).isPresent()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 존재하는 사물함 번호입니다.");
        }
        Locker locker = new Locker(request.getBuilding(), request.getFloor(),
                request.getLocationDetail(), request.getLockerNumber());
        lockerRepository.save(locker);
        log.info("[Admin] Locker added: {}", request.getLockerNumber());
        return AdminLockerResponse.of(locker, null);
    }

    public void changeLockerStatus(Long lockerId, LockerStatus newStatus) {
        Locker locker = getLocker(lockerId);

        if (newStatus == LockerStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "ACTIVE 상태는 예약을 통해서만 변경됩니다.");
        }

        // 사용중인 사물함을 BROKEN/DISABLED로 변경하면 강제 반납 처리
        if (locker.getStatus() == LockerStatus.ACTIVE) {
            forceReturnByLockerId(lockerId);
        }

        switch (newStatus) {
            case BROKEN -> locker.markBroken();
            case DISABLED -> locker.disable();
            case NORMAL -> locker.restore();
        }
        log.info("[Admin] Locker {} status changed to {}", lockerId, newStatus);
    }

    public void forceReturnByLockerId(Long lockerId) {
        Locker locker = getLocker(lockerId);
        LockerReservation reservation = lockerReservationRepository
                .findByLocker_IdAndStatus(lockerId, ReservationStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_ACTIVE_RESERVATION));
        reservation.returnReservation();
        locker.release();
        log.info("[Admin] Force returned locker {}", lockerId);
    }

    public void assignUser(Long lockerId, String studentNumber) {
        Locker locker = getLocker(lockerId);

        // BROKEN/DISABLED 사물함은 배정 불가 (ACTIVE·NORMAL만 허용)
        if (locker.getStatus() != LockerStatus.ACTIVE && !locker.isNormal()) {
            throw new BusinessException(ErrorCode.LOCKER_NOT_NORMAL);
        }

        // 기존 예약 있으면 강제 반납
        lockerReservationRepository.findByLocker_IdAndStatus(lockerId, ReservationStatus.ACTIVE)
                .ifPresent(r -> {
                    r.returnReservation();
                    locker.release();
                });

        User user = userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 해당 학생이 이미 다른 사물함 사용 중이면 반납
        lockerReservationRepository
                .findByUser_IdAndStatusAndExpiredAtAfter(user.getId(), ReservationStatus.ACTIVE, LocalDateTime.now())
                .ifPresent(r -> {
                    r.returnReservation();
                    r.getLocker().release();
                });

        if (!locker.isNormal()) {
            throw new BusinessException(ErrorCode.LOCKER_NOT_NORMAL);
        }

        LockerReservation newReservation = new LockerReservation(user, locker);
        lockerReservationRepository.save(newReservation);
        locker.occupy(newReservation.getExpiredAt());
        log.info("[Admin] Locker {} assigned to {}", lockerId, studentNumber);
    }

    public void bulkChangeStatus(List<Long> lockerIds, LockerStatus newStatus) {
        if (newStatus == LockerStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "ACTIVE 상태는 예약을 통해서만 변경됩니다.");
        }
        lockerRepository.findAllByIdIn(lockerIds).forEach(locker -> {
            if (locker.getStatus() == LockerStatus.ACTIVE) {
                lockerReservationRepository.findByLocker_IdAndStatus(locker.getId(), ReservationStatus.ACTIVE)
                        .ifPresent(r -> r.returnReservation());
                locker.release();
            }
            switch (newStatus) {
                case BROKEN -> locker.markBroken();
                case DISABLED -> locker.disable();
                case NORMAL -> locker.restore();
            }
        });
        log.info("[Admin] Bulk status change {} lockers to {}", lockerIds.size(), newStatus);
    }

    public void bulkForceReturn(List<Long> lockerIds) {
        lockerRepository.findAllByIdIn(lockerIds).forEach(locker -> {
            lockerReservationRepository.findByLocker_IdAndStatus(locker.getId(), ReservationStatus.ACTIVE)
                    .ifPresent(r -> {
                        r.returnReservation();
                        locker.release();
                    });
        });
        log.info("[Admin] Bulk force return {} lockers", lockerIds.size());
    }

    private Locker getLocker(Long lockerId) {
        return lockerRepository.findById(lockerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_NOT_FOUND));
    }

    private boolean matchesSearch(AdminLockerResponse response, String search) {
        if (search == null || search.isBlank()) return true;
        String q = search.toLowerCase();
        if (response.getLockerNumber().toLowerCase().contains(q)) return true;
        if (response.getCurrentUser() != null) {
            return response.getCurrentUser().getName().toLowerCase().contains(q)
                    || response.getCurrentUser().getStudentNumber().contains(q);
        }
        return false;
    }
}
