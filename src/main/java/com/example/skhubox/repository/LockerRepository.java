package com.example.skhubox.repository;

import com.example.skhubox.domain.locker.Locker;
import com.example.skhubox.domain.locker.LockerStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

import java.util.Optional;

public interface LockerRepository extends JpaRepository<Locker, Long> {

    Optional<Locker> findByLockerNumber(String lockerNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Locker l where l.id = :lockerId")
    Optional<Locker> findByIdWithPessimisticLock(Long lockerId);

    long countByStatus(LockerStatus status);

    @Query("SELECT l.building, COUNT(l), SUM(CASE WHEN l.status = 'ACTIVE' THEN 1 ELSE 0 END) FROM Locker l GROUP BY l.building ORDER BY l.building ASC")
    List<Object[]> countGroupByBuilding();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Locker l SET l.expiredAt = :newExpiry WHERE l.status = :status")
    int bulkUpdateExpiredAt(LockerStatus status, LocalDateTime newExpiry);
}