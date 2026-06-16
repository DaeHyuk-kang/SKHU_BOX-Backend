package com.example.skhubox.dto.admin;

import com.example.skhubox.domain.locker.Locker;
import com.example.skhubox.domain.reservation.LockerReservation;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
public class AdminLockerResponse {

    private final Long lockerId;
    private final String lockerNumber;
    private final String building;
    private final int floor;
    private final String locationDetail;
    private final String status;
    private final UserInfo currentUser;

    private AdminLockerResponse(Locker locker, LockerReservation reservation) {
        this.lockerId = locker.getId();
        this.lockerNumber = locker.getLockerNumber();
        this.building = locker.getBuilding();
        this.floor = locker.getFloor();
        this.locationDetail = locker.getLocationDetail();
        this.status = locker.getStatus().name();
        this.currentUser = reservation != null && reservation.getUser() != null
                ? new UserInfo(reservation) : null;
    }

    public static AdminLockerResponse of(Locker locker, LockerReservation reservation) {
        return new AdminLockerResponse(locker, reservation);
    }

    @Getter
    public static class UserInfo {
        private final Long userId;
        private final String studentNumber;
        private final String name;
        private final String department;
        private final Long reservationId;
        private final LocalDateTime reservedAt;
        private final LocalDateTime expiredAt;
        private final long daysLeft;

        public UserInfo(LockerReservation reservation) {
            this.userId = reservation.getUser().getId();
            this.studentNumber = reservation.getUser().getStudentNumber();
            this.name = reservation.getUser().getName();
            this.department = reservation.getUser().getDepartment();
            this.reservationId = reservation.getId();
            this.reservedAt = reservation.getReservedAt();
            this.expiredAt = reservation.getExpiredAt();
            this.daysLeft = ChronoUnit.DAYS.between(LocalDateTime.now(), reservation.getExpiredAt());
        }
    }
}
