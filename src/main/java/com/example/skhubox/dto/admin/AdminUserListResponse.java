package com.example.skhubox.dto.admin;

import com.example.skhubox.domain.reservation.LockerReservation;
import com.example.skhubox.domain.reservation.ReservationStatus;
import com.example.skhubox.domain.user.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminUserListResponse {

    private final Long userId;
    private final String studentNumber;
    private final String name;
    private final String email;
    private final String department;
    private final String role;
    private final ReservationInfo activeReservation;
    private final LocalDateTime createdAt;

    private AdminUserListResponse(User user, LockerReservation reservation) {
        this.userId = user.getId();
        this.studentNumber = user.getStudentNumber();
        this.name = user.getName();
        this.email = user.getEmail();
        this.department = user.getDepartment();
        this.role = user.getRole().name();
        this.activeReservation = reservation != null ? new ReservationInfo(reservation) : null;
        this.createdAt = user.getCreatedAt();
    }

    public static AdminUserListResponse of(User user, LockerReservation reservation) {
        return new AdminUserListResponse(user, reservation);
    }

    @Getter
    public static class ReservationInfo {
        private final Long reservationId;
        private final Long lockerId;
        private final String lockerNumber;
        private final String building;
        private final String status;
        private final LocalDateTime expiredAt;

        public ReservationInfo(LockerReservation reservation) {
            this.reservationId = reservation.getId();
            this.lockerId = reservation.getLocker().getId();
            this.lockerNumber = reservation.getLocker().getLockerNumber();
            this.building = reservation.getLocker().getBuilding();
            this.status = reservation.getStatus().name();
            this.expiredAt = reservation.getExpiredAt();
        }
    }
}
