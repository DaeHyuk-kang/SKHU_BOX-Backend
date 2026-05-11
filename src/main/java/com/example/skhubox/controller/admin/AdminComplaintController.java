package com.example.skhubox.controller.admin;

import com.example.skhubox.dto.ApiResponse;
import com.example.skhubox.dto.complaint.ComplaintAnswerRequest;
import com.example.skhubox.dto.complaint.ComplaintResponse;
import com.example.skhubox.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Complaint", description = "관리자 민원 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/complaints")
@PreAuthorize("hasRole('ADMIN')")
public class AdminComplaintController {

    private final ComplaintService complaintService;

    @Operation(summary = "전체 민원 목록 조회", description = "모든 사용자가 접수한 민원 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getAllComplaints() {
        List<ComplaintResponse> response = complaintService.getAllComplaints();
        return ResponseEntity.ok(ApiResponse.ok("전체 민원 목록 조회 성공", response));
    }

    @Operation(summary = "민원 답변 및 상태 변경", description = "특정 민원에 대한 답변을 작성하고 상태를 변경합니다.")
    @PatchMapping("/{complaintId}")
    public ResponseEntity<ApiResponse<ComplaintResponse>> answerComplaint(
            @PathVariable Long complaintId,
            @RequestBody @Valid ComplaintAnswerRequest request
    ) {
        ComplaintResponse response = complaintService.answerComplaint(complaintId, request);
        return ResponseEntity.ok(ApiResponse.ok("민원 답변이 등록되었습니다.", response));
    }
}
