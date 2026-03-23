package com.example.spacehub.service;

import com.example.spacehub.dto.LockerReservationResponse;

public interface LockerReservationService {

    LockerReservationResponse reserveLocker(Long userId, Long lockerId);

    LockerReservationResponse returnLocker(Long userId);

    LockerReservationResponse changeLocker(Long userId, Long newLockerId);
}