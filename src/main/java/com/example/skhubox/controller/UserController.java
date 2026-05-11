package com.example.skhubox.controller;

import com.example.skhubox.dto.ApiResponse;
import com.example.skhubox.dto.ChangePasswordRequest;
import com.example.skhubox.dto.FcmTokenRequest;
import com.example.skhubox.dto.NotificationSettingRequest;
import com.example.skhubox.dto.NotificationSettingResponse;
import com.example.skhubox.dto.UpdateProfileRequest;
import com.example.skhubox.dto.UserInfoResponse;
import com.example.skhubox.service.UserService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User API", description = "사용자 정보 관련 API")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 이름, 학번, 학부, 이메일, 가입일을 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getMyInfo(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                "사용자 정보 조회 성공",
                userService.getUserInfo(userDetails.getUsername())
        ));
    }

    @Operation(summary = "FCM 토큰 등록", description = "푸시 알림을 위한 기기 토큰을 등록합니다.")
    @PostMapping("/fcm-token")
    public ResponseEntity<ApiResponse<Void>> updateFcmToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody FcmTokenRequest request) {
        
        userService.updateFcmToken(userDetails.getUsername(), request.getToken());
        return ResponseEntity.ok(ApiResponse.ok("FCM 토큰이 성공적으로 등록되었습니다.", null));
    }

    @Operation(summary = "알림 설정 변경", description = "사용자의 알림 수신 여부를 변경합니다.")
    @PatchMapping("/notification-setting")
    public ResponseEntity<ApiResponse<NotificationSettingResponse>> updateNotificationSetting(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody NotificationSettingRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "알림 설정이 변경되었습니다.",
                userService.updateNotificationSetting(userDetails.getUsername(), request.isEnabled())
        ));
    }

    @Operation(summary = "프로필 수정", description = "이름, 학부를 변경합니다.")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "프로필이 수정되었습니다.",
                userService.updateProfile(userDetails.getUsername(), request)
        ));
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호 확인 후 새 비밀번호로 변경합니다.")
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("비밀번호가 변경되었습니다.", null));
    }

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 진행합니다. 사용 중인 사물함은 자동으로 반납됩니다.")
    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.withdrawUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("회원 탈퇴가 완료되었습니다.", null));
    }
}
