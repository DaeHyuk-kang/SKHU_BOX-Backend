package com.example.skhubox.repository;

import com.example.skhubox.domain.reservation.LockerReservation;
import com.example.skhubox.domain.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LockerReservationRepository extends JpaRepository<LockerReservation, Long> {

    boolean existsByUser_IdAndStatus(Long userId, ReservationStatus status);

    boolean existsByUser_IdAndStatusAndExpiredAtAfter(Long userId, ReservationStatus status, LocalDateTime expiredAt);

    boolean existsByLocker_IdAndStatus(Long lockerId, ReservationStatus status);

    boolean existsByLocker_IdAndStatusAndExpiredAtAfter(Long lockerId, ReservationStatus status, LocalDateTime expiredAt);

    Optional<LockerReservation> findByUser_IdAndStatus(Long userId, ReservationStatus status);

    Optional<LockerReservation> findByUser_IdAndStatusAndExpiredAtAfter(Long userId, ReservationStatus status, LocalDateTime expiredAt);

    Optional<LockerReservation> findByLocker_IdAndStatus(Long lockerId, ReservationStatus status);

    List<LockerReservation> findAllByStatus(ReservationStatus status);

    List<LockerReservation> findAllByStatusAndExpiredAtBefore(ReservationStatus status, LocalDateTime expiredAt);

    List<LockerReservation> findTop4ByStatusInOrderByCreatedAtDesc(List<ReservationStatus> statuses);

    long countByStatusIn(List<ReservationStatus> statuses);

    long countByStatusInAndExpiredAtBetween(List<ReservationStatus> statuses, LocalDateTime start, LocalDateTime end);

    long countByLocker_IdAndStatus(Long lockerId, ReservationStatus status);

    List<LockerReservation> findAllByUser_IdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT MIN(r.expiredAt) FROM LockerReservation r WHERE r.status = :status")
    Optional<LocalDateTime> findMinExpiredAtByStatus(ReservationStatus status);

    long countByStatusAndExpiredAtGreaterThanEqualAndExpiredAtLessThanEqual(
            ReservationStatus status, LocalDateTime from, LocalDateTime to);
}
