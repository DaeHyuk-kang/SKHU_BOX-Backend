package com.example.skhubox.controller.admin;

import com.example.skhubox.dto.ApiResponse;
import com.example.skhubox.dto.admin.QueueModeResponse;
import com.example.skhubox.dto.admin.QueueModeUpdateRequest;
import com.example.skhubox.service.QueueModeSettingService;
import com.example.skhubox.service.WaitingQueueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Queue Mode", description = "관리자 대기열 모드 설정 API")
@RestController
@RequestMapping("/api/admin/queue-mode")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminQueueModeController {

    private final QueueModeSettingService queueModeSettingService;
    private final WaitingQueueService waitingQueueService;

    @GetMapping
    public ResponseEntity<ApiResponse<QueueModeResponse>> getQueueMode() {
        QueueModeResponse response = queueModeSettingService.getQueueMode();
        return ResponseEntity.ok(ApiResponse.ok("대기열 모드 조회 성공", response));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<QueueModeResponse>> updateQueueMode(
            @RequestBody QueueModeUpdateRequest request
    ) {
        QueueModeResponse response = queueModeSettingService.updateQueueMode(request.isEnabled());
        String message = request.isEnabled()
                ? "대기열 모드가 활성화되었습니다."
                : "대기열 모드가 비활성화되었습니다.";

        return ResponseEntity.ok(ApiResponse.ok(message, response));
    }

    @PostMapping("/{lockerId}/skip")
    public ResponseEntity<ApiResponse<Void>> skipFirstUser(@PathVariable Long lockerId) {
        waitingQueueService.skipFirstUser(lockerId);
        return ResponseEntity.ok(ApiResponse.ok("대기열 1순위 사용자를 스킵했습니다.", null));
    }
}