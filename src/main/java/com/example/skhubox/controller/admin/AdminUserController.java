package com.example.skhubox.controller.admin;

import com.example.skhubox.dto.ApiResponse;
import com.example.skhubox.dto.admin.AdminUserListResponse;
import com.example.skhubox.dto.admin.UserRoleUpdateRequest;
import com.example.skhubox.security.CustomUserDetails;
import com.example.skhubox.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin User", description = "관리자용 사용자 관리 API")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @Operation(summary = "사용자 권한 변경", description = "특정 사용자의 권한을 ADMIN 또는 USER로 변경합니다.")
    @PatchMapping("/role")
    public ResponseEntity<ApiResponse<Void>> updateUserRole(
            @Valid @RequestBody UserRoleUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails adminDetails
    ) {
        String adminStudentNumber = adminDetails.getUsername();
        userService.updateUserRole(adminStudentNumber, request.getTargetStudentNumber(), request.getRole());
        
        String message = request.getRole().name() + " 권한으로 변경되었습니다.";
        return ResponseEntity.ok(ApiResponse.ok(message, null));
    }

    @Operation(summary = "사용자 강제 탈퇴", description = "특정 사용자를 강제로 탈퇴 처리합니다. 관리자 계정이나 본인은 탈퇴 불가합니다.")
    @DeleteMapping("/{studentNumber}")
    public ResponseEntity<ApiResponse<Void>> withdrawUser(
            @PathVariable String studentNumber,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails adminDetails) {
        userService.adminWithdrawUser(adminDetails.getUsername(), studentNumber);
        return ResponseEntity.ok(ApiResponse.ok("사용자가 강제 탈퇴 처리되었습니다.", null));
    }

    @Operation(summary = "전체 학생 조회 및 검색", description = "전체 학생 목록과 예약 중인 사물함 정보를 반환합니다. name 또는 studentNumber로 검색 가능합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminUserListResponse>>> getAllUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String studentNumber) {
        return ResponseEntity.ok(ApiResponse.ok("전체 학생 목록 조회 성공", userService.getAllUsers(name, studentNumber)));
    }

    @Operation(summary = "관리자 목록 조회", description = "ADMIN 권한을 가진 사용자 목록을 반환합니다.")
    @GetMapping("/admins")
    public ResponseEntity<ApiResponse<List<AdminUserListResponse>>> getAllAdmins() {
        return ResponseEntity.ok(ApiResponse.ok("관리자 목록 조회 성공", userService.getAllAdmins()));
    }
}
