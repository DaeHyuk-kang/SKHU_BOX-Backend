package com.example.spacehub.controller;

import com.example.spacehub.dto.ApiResponse;
import com.example.spacehub.dto.LockerChangeRequest;
import com.example.spacehub.dto.LockerReservationResponse;
import com.example.spacehub.dto.LockerReserveRequest;
import com.example.spacehub.dto.LockerReturnRequest;
import com.example.spacehub.service.LockerReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lockers")
public class LockerReservationController {

    private final LockerReservationService lockerReservationService;

    public LockerReservationController(LockerReservationService lockerReservationService) {
        this.lockerReservationService = lockerReservationService;
    }

    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<LockerReservationResponse>> reserveLocker(
            @RequestBody LockerReserveRequest request
    ) {
        LockerReservationResponse response =
                lockerReservationService.reserveLocker(request.getUserId(), request.getLockerId());

        return ResponseEntity.ok(ApiResponse.ok("사물함 예약 성공", response));
    }

    @PostMapping("/return")
    public ResponseEntity<ApiResponse<LockerReservationResponse>> returnLocker(
            @RequestBody LockerReturnRequest request
    ) {
        LockerReservationResponse response =
                lockerReservationService.returnLocker(request.getUserId());

        return ResponseEntity.ok(ApiResponse.ok("사물함 반납 성공", response));
    }

    @PostMapping("/change")
    public ResponseEntity<ApiResponse<LockerReservationResponse>> changeLocker(
            @RequestBody LockerChangeRequest request
    ) {
        LockerReservationResponse response =
                lockerReservationService.changeLocker(
                        request.getUserId(),
                        request.getNewLockerId()
                );

        return ResponseEntity.ok(ApiResponse.ok("사물함 변경 성공", response));
    }
}