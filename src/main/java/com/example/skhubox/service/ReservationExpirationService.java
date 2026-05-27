
package com.example.skhubox.service;

import com.example.skhubox.domain.reservation.LockerReservation;
import com.example.skhubox.domain.reservation.ReservationStatus;
import com.example.skhubox.domain.operation.OperationLogType;
import com.example.skhubox.repository.LockerReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationExpirationService {

    private final LockerReservationRepository lockerReservationRepository;
    private final OperationLogService operationLogService;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void expireOverdueReservations() {
        List<LockerReservation> overdueReservations = lockerReservationRepository
                .findAllByStatusAndExpiredAtBefore(ReservationStatus.ACTIVE, LocalDateTime.now());

        for (LockerReservation reservation : overdueReservations) {
            reservation.expire();
            reservation.getLocker().release();
            operationLogService.log(
                    OperationLogType.RESERVATION_EXPIRED,
                    "예약 만료 처리",
                    String.format("%s번 사물함 예약이 자동 만료 처리되었습니다.", reservation.getLocker().getLockerNumber())
            );
        }

        if (!overdueReservations.isEmpty()) {
            log.info("[Reservation-Expiry] Expired {} overdue reservations", overdueReservations.size());
        }
    }
}
