package com.example.skhubox.repository;

import com.example.skhubox.domain.reservation.LockerReservation;
import com.example.skhubox.domain.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LockerReservationRepository extends JpaRepository<LockerReservation, Long> {

    boolean existsByUser_IdAndStatus(Long userId, ReservationStatus status);

    boolean existsByUser_IdAndStatusAndExpiredAtAfter(Long userId, ReservationStatus status, LocalDateTime expiredAt);

    boolean existsByLocker_IdAndStatus(Long lockerId, ReservationStatus status);

    boolean existsByLocker_IdAndStatusAndExpiredAtAfter(Long lockerId, ReservationStatus status, LocalDateTime expiredAt);

    // 탈퇴 처리 시 만료된 ACTIVE 예약 조회 (locker fetch)
    @Query("SELECT r FROM LockerReservation r JOIN FETCH r.locker WHERE r.user.id = :userId AND r.status = :status")
    Optional<LockerReservation> findByUser_IdAndStatus(Long userId, ReservationStatus status);

    // 현재 예약 조회 - returnLocker/changeLocker/getMyReservation (locker + user fetch)
    @Query("SELECT r FROM LockerReservation r JOIN FETCH r.locker JOIN FETCH r.user WHERE r.user.id = :userId AND r.status = :status AND r.expiredAt > :expiredAt")
    Optional<LockerReservation> findByUser_IdAndStatusAndExpiredAtAfter(Long userId, ReservationStatus status, LocalDateTime expiredAt);

    // AdminLockerService - force return/assign (user fetch)
    @Query("SELECT r FROM LockerReservation r LEFT JOIN FETCH r.user WHERE r.locker.id = :lockerId AND r.status = :status")
    Optional<LockerReservation> findByLocker_IdAndStatus(Long lockerId, ReservationStatus status);

    // AdminLockerService.getLockers - 전체 ACTIVE 예약 맵 빌드 (locker + user fetch)
    @Query("SELECT r FROM LockerReservation r JOIN FETCH r.locker LEFT JOIN FETCH r.user WHERE r.status = :status")
    List<LockerReservation> findAllByStatus(ReservationStatus status);

    // ReservationExpirationService 스케줄러 - 만료 처리 (locker fetch)
    @Query("SELECT r FROM LockerReservation r JOIN FETCH r.locker WHERE r.status = :status AND r.expiredAt < :expiredAt")
    List<LockerReservation> findAllByStatusAndExpiredAtBefore(ReservationStatus status, LocalDateTime expiredAt);

    List<LockerReservation> findTop4ByStatusInOrderByCreatedAtDesc(List<ReservationStatus> statuses);

    long countByStatusIn(List<ReservationStatus> statuses);

    long countByStatusInAndExpiredAtBetween(List<ReservationStatus> statuses, LocalDateTime start, LocalDateTime end);

    long countByLocker_IdAndStatus(Long lockerId, ReservationStatus status);

    // 예약 이력 조회 (locker fetch)
    @Query("SELECT r FROM LockerReservation r JOIN FETCH r.locker WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<LockerReservation> findAllByUser_IdOrderByCreatedAtDesc(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE LockerReservation r SET r.user = null WHERE r.user.id = :userId")
    void nullifyUser(Long userId);

    @Query("SELECT MIN(r.expiredAt) FROM LockerReservation r WHERE r.status = :status")
    Optional<LocalDateTime> findMinExpiredAtByStatus(ReservationStatus status);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE LockerReservation r SET r.expiredAt = :newExpiry WHERE r.status = :status")
    int bulkUpdateExpiryDate(ReservationStatus status, LocalDateTime newExpiry);

    long countByStatusAndExpiredAtGreaterThanEqualAndExpiredAtLessThanEqual(
            ReservationStatus status, LocalDateTime from, LocalDateTime to);

    // 만료 임박 예약 목록 (locker + user fetch)
    @Query("SELECT r FROM LockerReservation r JOIN FETCH r.locker LEFT JOIN FETCH r.user WHERE r.status = :status AND r.expiredAt BETWEEN :from AND :to ORDER BY r.expiredAt ASC")
    List<LockerReservation> findAllByStatusAndExpiredAtBetweenOrderByExpiredAtAsc(
            ReservationStatus status, LocalDateTime from, LocalDateTime to);
}
