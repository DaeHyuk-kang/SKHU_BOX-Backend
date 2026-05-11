package com.example.skhubox.controller;

import com.example.skhubox.dto.ApiResponse;
import com.example.skhubox.dto.LockerChangeRequest;
import com.example.skhubox.dto.LockerReservationResponse;
import com.example.skhubox.dto.LockerResponse;
import com.example.skhubox.dto.LockerReserveRequest;
import com.example.skhubox.dto.QueueResponse;
import com.example.skhubox.dto.ReservationHistoryResponse;
import com.example.skhubox.security.CustomUserDetails;
import com.example.skhubox.service.LockerReservationService;
import com.example.skhubox.service.WaitingQueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Locker API", description = "사물함 예약 관련 API")
@RestController
@RequestMapping("/api/lockers")
@RequiredArgsConstructor
public class LockerReservationController {

    private final LockerReservationService lockerReservationService;
    private final WaitingQueueService waitingQueueService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LockerResponse>>> getAllLockers() {
        List<LockerResponse> response = lockerReservationService.getAllLockers();
        return ResponseEntity.ok(ApiResponse.ok("전체 사물함 목록 조회 성공", response));
    }

    @Operation(summary = "내 예약 내역 조회", description = "현재 및 과거 사물함 예약 내역을 최신순으로 조회합니다.")
    @GetMapping("/my-history")
    public ResponseEntity<ApiResponse<List<ReservationHistoryResponse>>> getMyReservationHistory(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok("예약 내역 조회 성공",
                lockerReservationService.getMyReservationHistory(userDetails.getUsername())));
    }

    @GetMapping("/my-reservation")
    public ResponseEntity<ApiResponse<LockerReservationResponse>> getMyReservation(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String studentNumber = userDetails.getUsername();
        LockerReservationResponse response = lockerReservationService.getMyReservation(studentNumber);
        return ResponseEntity.ok(ApiResponse.ok("내 예약 정보 조회 성공", response));
    }

    @GetMapping("/queue/my-rank")
    public ResponseEntity<ApiResponse<QueueResponse>> getMyRank(
            @RequestParam Long lockerId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String studentNumber = userDetails.getUsername();
        Long rank = waitingQueueService.getRank(studentNumber, lockerId);
        if (rank == null) {
            return ResponseEntity.ok(ApiResponse.ok("대기열에 등록되어 있지 않습니다.", QueueResponse.of(lockerId, null, "대기열 미등록")));
        }
        return ResponseEntity.ok(ApiResponse.ok("내 순번 조회 성공", QueueResponse.of(lockerId, rank, "현재 순번: " + rank)));
    }

    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<LockerReservationResponse>> reserveLocker(
            @RequestBody LockerReserveRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String studentNumber = userDetails.getUsername();

        LockerReservationResponse response =
                lockerReservationService.reserveLocker(studentNumber, request.getLockerId());

        return ResponseEntity.ok(ApiResponse.ok("사물함 예약 성공", response));
    }

    @PostMapping("/return")
    public ResponseEntity<ApiResponse<LockerReservationResponse>> returnLocker(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String studentNumber = userDetails.getUsername();
        LockerReservationResponse response =
                lockerReservationService.returnLocker(studentNumber);

        return ResponseEntity.ok(ApiResponse.ok("사물함 반납 성공", response));
    }

    @PostMapping("/change")
    public ResponseEntity<ApiResponse<LockerReservationResponse>> changeLocker(
            @RequestBody LockerChangeRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String studentNumber = userDetails.getUsername();
        LockerReservationResponse response =
                lockerReservationService.changeLocker(studentNumber, request.getNewLockerId());

        return ResponseEntity.ok(ApiResponse.ok("사물함 변경 성공", response));
    }
}
