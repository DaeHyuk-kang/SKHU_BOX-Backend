package com.example.skhubox.service;

import com.example.skhubox.dto.LockerReservationResponse;

public interface LockerReservationService {

    LockerReservationResponse reserveLocker(String studentNumber, Long lockerId);

    LockerReservationResponse returnLocker(String studentNumber);

    LockerReservationResponse changeLocker(String studentNumber, Long newLockerId);
}
