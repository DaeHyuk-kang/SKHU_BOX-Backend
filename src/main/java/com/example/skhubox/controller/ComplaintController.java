package com.example.skhubox.controller;

import com.example.skhubox.dto.ApiResponse;
import com.example.skhubox.dto.complaint.ComplaintRequest;
import com.example.skhubox.dto.complaint.ComplaintResponse;
import com.example.skhubox.security.CustomUserDetails;
import com.example.skhubox.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Complaint", description = "민원 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    @Operation(summary = "민원 작성", description = "사물함 번호와 문의 내용을 입력하여 민원을 작성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<ComplaintResponse>> createComplaint(
            @RequestBody @Valid ComplaintRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ComplaintResponse response = complaintService.createComplaint(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("민원이 성공적으로 접수되었습니다.", response));
    }

    @Operation(summary = "내 민원 목록 조회", description = "로그인한 사용자가 작성한 민원 목록을 조회합니다.")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getMyComplaints(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<ComplaintResponse> response = complaintService.getMyComplaints(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("내 민원 목록 조회 성공", response));
    }

    @Operation(summary = "민원 상세 조회", description = "민원 ID로 특정 민원의 상세 내용을 조회합니다. 본인 민원만 조회 가능합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaintDetail(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok("민원 상세 조회 성공",
                complaintService.getComplaintDetail(userDetails.getUsername(), id)));
    }

    @Operation(summary = "민원 취소", description = "대기중 상태인 본인 민원을 취소합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelComplaint(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        complaintService.cancelComplaint(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.ok("민원이 취소되었습니다.", null));
    }
}
