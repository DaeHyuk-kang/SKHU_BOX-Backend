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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locker_id", nullable = false)
    private Locker locker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private LocalDateTime reservedAt;

    private LocalDateTime endAt;

    public LockerReservation(User user, Locker locker) {
        this.user = user;
        this.locker = locker;
        this.status = ReservationStatus.ACTIVE;
        this.reservedAt = LocalDateTime.now();
    }

    public void returnReservation() {
        this.status = ReservationStatus.RETURNED;
        this.endAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = ReservationStatus.EXPIRED;
    }
}