package com.example.skhubox.controller.admin;

import com.example.skhubox.dto.ApiResponse;
import com.example.skhubox.dto.admin.*;
import com.example.skhubox.service.AdminLockerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Locker", description = "관리자용 사물함 관리 API")
@RestController
@RequestMapping("/api/admin/lockers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLockerController {

    private final AdminLockerService adminLockerService;

    @Operation(summary = "사물함 목록 조회", description = "건물/층 필터 및 사물함 번호·학생 이름 검색을 지원합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminLockerResponse>>> getLockers(
            @RequestParam(required = false) String building,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.ok("사물함 목록 조회 성공",
                adminLockerService.getLockers(building, floor, search)));
    }

    @Operation(summary = "사물함 추가")
    @PostMapping
    public ResponseEntity<ApiResponse<AdminLockerResponse>> addLocker(
            @Valid @RequestBody AdminLockerAddRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("사물함이 추가되었습니다.", adminLockerService.addLocker(request)));
    }

    @Operation(summary = "사물함 상태 변경", description = "NORMAL / BROKEN / DISABLED 로 변경 가능합니다. 사용중인 사물함을 변경하면 강제 반납됩니다.")
    @PatchMapping("/{lockerId}/status")
    public ResponseEntity<ApiResponse<Void>> changeStatus(
            @PathVariable Long lockerId,
            @Valid @RequestBody AdminLockerStatusRequest request) {
        adminLockerService.changeLockerStatus(lockerId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.ok("사물함 상태가 변경되었습니다.", null));
    }

    @Operation(summary = "사물함 강제 반납", description = "특정 사물함을 강제 반납 처리합니다.")
    @PostMapping("/{lockerId}/force-return")
    public ResponseEntity<ApiResponse<Void>> forceReturn(@PathVariable Long lockerId) {
        adminLockerService.forceReturnByLockerId(lockerId);
        return ResponseEntity.ok(ApiResponse.ok("강제 반납 처리되었습니다.", null));
    }

    @Operation(summary = "사용자 변경", description = "특정 사물함의 사용자를 변경합니다. 기존 예약은 반납 처리됩니다.")
    @PostMapping("/{lockerId}/assign")
    public ResponseEntity<ApiResponse<Void>> assignUser(
            @PathVariable Long lockerId,
            @Valid @RequestBody AdminLockerAssignRequest request) {
        adminLockerService.assignUser(lockerId, request.getStudentNumber());
        return ResponseEntity.ok(ApiResponse.ok("사용자가 변경되었습니다.", null));
    }

    @Operation(summary = "일괄 상태 변경", description = "선택한 사물함들의 상태를 일괄 변경합니다.")
    @PatchMapping("/bulk/status")
    public ResponseEntity<ApiResponse<Void>> bulkChangeStatus(
            @Valid @RequestBody AdminLockerBulkRequest request) {
        adminLockerService.bulkChangeStatus(request.getLockerIds(), request.getStatus());
        return ResponseEntity.ok(ApiResponse.ok(request.getLockerIds().size() + "개 사물함 상태가 변경되었습니다.", null));
    }

    @Operation(summary = "일괄 강제 반납", description = "선택한 사물함들을 일괄 강제 반납 처리합니다.")
    @PostMapping("/bulk/force-return")
    public ResponseEntity<ApiResponse<Void>> bulkForceReturn(
            @Valid @RequestBody AdminLockerBulkRequest request) {
        adminLockerService.bulkForceReturn(request.getLockerIds());
        return ResponseEntity.ok(ApiResponse.ok(request.getLockerIds().size() + "개 사물함이 강제 반납되었습니다.", null));
    }
}
