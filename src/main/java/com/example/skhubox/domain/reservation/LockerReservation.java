package com.example.skhubox.domain.reservation;

import com.example.skhubox.domain.common.BaseEntity;
import com.example.skhubox.domain.locker.Locker;
import com.example.skhubox.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "locker_reservations")
public class LockerReservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locker_id", nullable = false)
    private Locker locker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private LocalDateTime reservedAt;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    private LocalDateTime endAt;

    public LockerReservation(User user, Locker locker) {
        this.user = user;
        this.locker = locker;
        this.status = ReservationStatus.ACTIVE;
        this.reservedAt = LocalDateTime.now();
        this.expiredAt = calculateExpiryDate(this.reservedAt);
    }

    private LocalDateTime calculateExpiryDate(LocalDateTime start) {
        if (start.getMonthValue() <= 6) {
            // 1학기: 7월 1일 00:00
            return LocalDateTime.of(start.getYear(), 7, 1, 0, 0);
        } else {
            // 2학기: 내년 1월 1일 00:00
            return LocalDateTime.of(start.getYear() + 1, 1, 1, 0, 0);
        }
    }

    public void returnReservation() {
        this.status = ReservationStatus.RETURNED;
        this.endAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = ReservationStatus.EXPIRED;
    }

    public void updateExpiryDate(LocalDateTime newExpiryDate) {
        this.expiredAt = newExpiryDate;
    }
}