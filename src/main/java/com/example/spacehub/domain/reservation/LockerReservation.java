package com.example.spacehub.domain.reservation;

import com.example.spacehub.domain.locker.Locker;
import com.example.spacehub.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "locker_reservations")
public class LockerReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "locker_id", nullable = false)
    private Locker locker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(nullable = false)
    private LocalDateTime reservedAt;

    private LocalDateTime returnedAt;

    public LockerReservation(User user, Locker locker) {
        this.user = user;
        this.locker = locker;
        this.status = ReservationStatus.ACTIVE;
        this.reservedAt = LocalDateTime.now();
    }

    public void returnReservation() {
        this.status = ReservationStatus.RETURNED;
        this.returnedAt = LocalDateTime.now();
    }

    public void cancelReservation() {
        this.status = ReservationStatus.CANCELLED;
        this.returnedAt = LocalDateTime.now();
    }

    public void expireReservation() {
        this.status = ReservationStatus.EXPIRED;
        this.returnedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == ReservationStatus.ACTIVE;
    }
}