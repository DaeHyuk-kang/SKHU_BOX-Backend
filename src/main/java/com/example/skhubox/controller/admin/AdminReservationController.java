package com.example.skhubox.controller.admin;

import com.example.skhubox.dto.ApiResponse;
import com.example.skhubox.dto.admin.ExpiringReservationResponse;
import com.example.skhubox.service.LockerReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Admin Reservation API", description = "관리자용 예약 관리 API")
@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReservationController {

    private final LockerReservationService lockerReservationService;

    @Operation(summary = "전체 예약 만료일 일괄 수정", description = "현재 사용 중인 모든 사물함의 만료 기한을 한 번에 수정합니다.")
    @PatchMapping("/expiry")
    public ResponseEntity<ApiResponse<Void>> updateAllExpirations(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime newExpiryDate) {
        lockerReservationService.updateAllActiveExpirations(newExpiryDate);
        return ResponseEntity.ok(ApiResponse.ok("모든 예약의 만료일이 성공적으로 수정되었습니다.", null));
    }

    @Operation(summary = "만료 임박 예약 목록 조회", description = "지정한 일수 이내에 만료되는 예약 목록을 반환합니다. 기본값 7일.")
    @GetMapping("/expiring")
    public ResponseEntity<ApiResponse<List<ExpiringReservationResponse>>> getExpiringReservations(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(ApiResponse.ok("만료 임박 예약 목록 조회 성공", lockerReservationService.getExpiringReservations(days)));
    }

    @Operation(summary = "사용자 사물함 강제 반납", description = "특정 학생의 사물함을 관리자가 강제로 반납 처리합니다.")
    @PostMapping("/force-return/{studentNumber}")
    public ResponseEntity<ApiResponse<Void>> forceReturn(@PathVariable String studentNumber) {
        lockerReservationService.returnLocker(studentNumber);
        return ResponseEntity.ok(ApiResponse.ok(studentNumber + " 사용자의 사물함이 강제 반납되었습니다.", null));
    }
}
