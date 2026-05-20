package com.example.skhubox.controller.admin;

import com.example.skhubox.dto.ApiResponse;
import com.example.skhubox.service.LockerReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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
}
