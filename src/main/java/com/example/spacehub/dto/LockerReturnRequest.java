package com.example.spacehub.dto;

public class LockerReturnRequest {

    private Long userId;
    private Long lockerId;

    public LockerReturnRequest() {
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
}