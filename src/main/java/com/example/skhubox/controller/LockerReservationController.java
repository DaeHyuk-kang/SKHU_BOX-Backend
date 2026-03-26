package com.example.skhubox.controller;

import com.example.skhubox.dto.ApiResponse;
import com.example.skhubox.dto.LockerChangeRequest;
import com.example.skhubox.dto.LockerReservationResponse;
import com.example.skhubox.dto.LockerReserveRequest;
import com.example.skhubox.service.LockerReservationService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
            @RequestBody LockerReserveRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        String studentNumber = authentication.getName();

        LockerReservationResponse response =
                lockerReservationService.reserveLocker(studentNumber, request.getLockerId());

        return ResponseEntity.ok(ApiResponse.ok("사물함 예약 성공", response));
    }

    @PostMapping("/return")
    public ResponseEntity<ApiResponse<LockerReservationResponse>> returnLocker(
            @Parameter(hidden = true) Authentication authentication
    ) {
        String studentNumber = authentication.getName();
        LockerReservationResponse response =
                lockerReservationService.returnLocker(studentNumber);

        return ResponseEntity.ok(ApiResponse.ok("사물함 반납 성공", response));
    }

    @PostMapping("/change")
    public ResponseEntity<ApiResponse<LockerReservationResponse>> changeLocker(
            @RequestBody LockerChangeRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        String studentNumber = authentication.getName();
        LockerReservationResponse response =
                lockerReservationService.changeLocker(studentNumber, request.getNewLockerId());

        return ResponseEntity.ok(ApiResponse.ok("사물함 변경 성공", response));
    }
}
