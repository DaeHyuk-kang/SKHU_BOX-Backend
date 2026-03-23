package com.example.spacehub.dto;

public class LockerChangeRequest {

    private Long userId;
    private Long currentLockerId;
    private Long newLockerId;

    public LockerChangeRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCurrentLockerId() {
        return currentLockerId;
    }

    public void setCurrentLockerId(Long currentLockerId) {
        this.currentLockerId = currentLockerId;
    }

    public Long getNewLockerId() {
        return newLockerId;
    }

    public void setNewLockerId(Long newLockerId) {
        this.newLockerId = newLockerId;
    }
}