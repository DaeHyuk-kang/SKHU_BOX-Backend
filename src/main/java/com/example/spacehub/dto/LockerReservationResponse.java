package com.example.spacehub.dto;

public class LockerReservationResponse {

    private Long reservationId;
    private Long userId;
    private Long lockerId;
    private String status;
    private String message;

    public LockerReservationResponse() {
    }

    public LockerReservationResponse(Long reservationId, Long userId, Long lockerId, String status, String message) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.lockerId = lockerId;
        this.status = status;
        this.message = message;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getLockerId() {
        return lockerId;
    }

    public void setLockerId(Long lockerId) {
        this.lockerId = lockerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}