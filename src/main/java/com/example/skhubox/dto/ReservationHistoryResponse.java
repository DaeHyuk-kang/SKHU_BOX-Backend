package com.example.skhubox.dto;

import com.example.skhubox.domain.reservation.LockerReservation;
import lombok.Getter;

@Getter
public class ReservationHistoryResponse {

    private final Long reservationId;
    private final Long lockerId;
    private final String lockerNumber;
    private final String building;
    private final String status;
    private final String reservedAt;
    private final String expiredAt;
    private final String endAt;

    private ReservationHistoryResponse(LockerReservation r) {
        this.reservationId = r.getId();
        this.lockerId = r.getLocker().getId();
        this.lockerNumber = r.getLocker().getLockerNumber();
        this.building = r.getLocker().getBuilding();
        this.status = r.getStatus().name();
        this.reservedAt = r.getReservedAt() != null ? r.getReservedAt().toString() : null;
        this.expiredAt = r.getExpiredAt() != null ? r.getExpiredAt().toString() : null;
        this.endAt = r.getEndAt() != null ? r.getEndAt().toString() : null;
    }

    public static ReservationHistoryResponse from(LockerReservation reservation) {
        return new ReservationHistoryResponse(reservation);
    }
}
