package com.example.spacehub.repository;

import com.example.spacehub.domain.reservation.LockerReservation;
import com.example.spacehub.domain.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LockerReservationRepository extends JpaRepository<LockerReservation, Long> {

    boolean existsByUser_IdAndStatus(Long userId, ReservationStatus status);

    boolean existsByLocker_IdAndStatus(Long lockerId, ReservationStatus status);

    Optional<LockerReservation> findByUser_IdAndStatus(Long userId, ReservationStatus status);

    Optional<LockerReservation> findByLocker_IdAndStatus(Long lockerId, ReservationStatus status);

    long countByLocker_IdAndStatus(Long lockerId, ReservationStatus status);
}