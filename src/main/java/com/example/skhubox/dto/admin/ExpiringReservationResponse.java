package com.example.skhubox.dto.admin;

import com.example.skhubox.domain.reservation.LockerReservation;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
public class ExpiringReservationResponse {

    private final Long reservationId;
    private final Long userId;
    private final String studentNumber;
    private final String name;
    private final String email;
    private final Long lockerId;
    private final String lockerNumber;
    private final String building;
    private final LocalDateTime expiredAt;
    private final long daysLeft;

    private ExpiringReservationResponse(LockerReservation reservation) {
        this.reservationId = reservation.getId();
        this.userId = reservation.getUser() != null ? reservation.getUser().getId() : null;
        this.studentNumber = reservation.getUser() != null ? reservation.getUser().getStudentNumber() : null;
        this.name = reservation.getUser() != null ? reservation.getUser().getName() : null;
        this.email = reservation.getUser() != null ? reservation.getUser().getEmail() : null;
        this.lockerId = reservation.getLocker().getId();
        this.lockerNumber = reservation.getLocker().getLockerNumber();
        this.building = reservation.getLocker().getBuilding();
        this.expiredAt = reservation.getExpiredAt();
        this.daysLeft = ChronoUnit.DAYS.between(LocalDateTime.now(), reservation.getExpiredAt());
    }

    public static ExpiringReservationResponse from(LockerReservation reservation) {
        return new ExpiringReservationResponse(reservation);
    }
}
