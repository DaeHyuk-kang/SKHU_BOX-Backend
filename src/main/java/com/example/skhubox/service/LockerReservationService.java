package com.example.skhubox.service;

import com.example.skhubox.dto.LockerReservationResponse;
import com.example.skhubox.dto.LockerResponse;
import com.example.skhubox.dto.ReservationHistoryResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface LockerReservationService {

    LockerReservationResponse reserveLocker(String studentNumber, Long lockerId);

    LockerReservationResponse returnLocker(String studentNumber);

    LockerReservationResponse changeLocker(String studentNumber, Long newLockerId);

    List<LockerResponse> getAllLockers();

    LockerReservationResponse getMyReservation(String studentNumber);

    List<ReservationHistoryResponse> getMyReservationHistory(String studentNumber);

    void updateExpiryDate(Long reservationId, LocalDateTime newExpiryDate);
    void updateAllActiveExpirations(LocalDateTime newExpiryDate);
}
