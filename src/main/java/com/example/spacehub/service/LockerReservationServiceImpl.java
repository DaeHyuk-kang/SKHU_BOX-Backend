package com.example.spacehub.service;

import com.example.spacehub.domain.locker.Locker;
import com.example.spacehub.domain.reservation.LockerReservation;
import com.example.spacehub.domain.reservation.ReservationStatus;
import com.example.spacehub.domain.user.User;
import com.example.spacehub.dto.LockerReservationResponse;
import com.example.spacehub.repository.LockerRepository;
import com.example.spacehub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.spacehub.repository.LockerReservationRepository;

@Service
@Transactional
public class LockerReservationServiceImpl implements LockerReservationService {

    private final UserRepository userRepository;
    private final LockerRepository lockerRepository;
    private final LockerReservationRepository lockerReservationRepository;

    public LockerReservationServiceImpl(UserRepository userRepository,
                                        LockerRepository lockerRepository,
                                        LockerReservationRepository lockerReservationRepository) {
        this.userRepository = userRepository;
        this.lockerRepository = lockerRepository;
        this.lockerReservationRepository = lockerReservationRepository;
    }

    @Override
    public LockerReservationResponse reserveLocker(Long userId, Long lockerId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Locker locker = lockerRepository.findByIdWithPessimisticLock(lockerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사물함입니다."));

        if (lockerReservationRepository.existsByUser_IdAndStatus(userId, ReservationStatus.ACTIVE)) {
            throw new IllegalArgumentException("이미 예약 중인 사물함이 있습니다.");
        }

        if (locker.isBrokenOrDisabled()) {
            throw new IllegalArgumentException("해당 사물함은 예약할 수 없습니다.");
        }

        if (!locker.isAvailable()) {
            throw new IllegalArgumentException("이미 예약된 사물함입니다.");
        }

        if (lockerReservationRepository.existsByLocker_IdAndStatus(lockerId, ReservationStatus.ACTIVE)) {
            throw new IllegalArgumentException("이미 사용 중인 사물함입니다.");
        }

        locker.reserve();

        LockerReservation reservation = new LockerReservation(user, locker);
        LockerReservation savedReservation = lockerReservationRepository.save(reservation);

        return new LockerReservationResponse(
                savedReservation.getId(),
                user.getId(),
                locker.getId(),
                savedReservation.getStatus().name(),
                "사물함 예약이 완료되었습니다."
        );
    }

    @Override
    public LockerReservationResponse returnLocker(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        LockerReservation reservation = lockerReservationRepository
                .findByUser_IdAndStatus(userId, ReservationStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("사용 중인 사물함이 없습니다."));

        Locker locker = lockerRepository.findByIdWithPessimisticLock(reservation.getLocker().getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사물함입니다."));

        reservation.returnReservation();
        locker.makeAvailable();

        return new LockerReservationResponse(
                reservation.getId(),
                user.getId(),
                locker.getId(),
                reservation.getStatus().name(),
                "사물함 반납이 완료되었습니다."
        );
    }

    @Override
    public LockerReservationResponse changeLocker(Long userId, Long newLockerId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        LockerReservation currentReservation = lockerReservationRepository
                .findByUser_IdAndStatus(userId, ReservationStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("현재 사용 중인 사물함이 없습니다."));

        Locker currentLocker = lockerRepository.findByIdWithPessimisticLock(currentReservation.getLocker().getId())
                .orElseThrow(() -> new IllegalArgumentException("현재 사물함이 존재하지 않습니다."));

        Locker newLocker = lockerRepository.findByIdWithPessimisticLock(newLockerId)
                .orElseThrow(() -> new IllegalArgumentException("새 사물함이 존재하지 않습니다."));

        if (currentLocker.getId().equals(newLockerId)) {
            throw new IllegalArgumentException("같은 사물함으로는 변경할 수 없습니다.");
        }

        if (newLocker.isBrokenOrDisabled()) {
            throw new IllegalArgumentException("새 사물함은 예약할 수 없습니다.");
        }

        if (!newLocker.isAvailable()) {
            throw new IllegalArgumentException("새 사물함은 이미 예약되어 있습니다.");
        }

        if (lockerReservationRepository.existsByLocker_IdAndStatus(newLockerId, ReservationStatus.ACTIVE)) {
            throw new IllegalArgumentException("새 사물함은 이미 사용 중입니다.");
        }

        currentReservation.returnReservation();
        currentLocker.makeAvailable();

        newLocker.reserve();
        LockerReservation newReservation = new LockerReservation(user, newLocker);
        LockerReservation savedReservation = lockerReservationRepository.save(newReservation);

        return new LockerReservationResponse(
                savedReservation.getId(),
                user.getId(),
                newLocker.getId(),
                savedReservation.getStatus().name(),
                "사물함 변경이 완료되었습니다."
        );
    }
}