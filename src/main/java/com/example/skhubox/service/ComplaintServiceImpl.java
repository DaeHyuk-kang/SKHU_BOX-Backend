package com.example.skhubox.service;

import com.example.skhubox.domain.complaint.Complaint;
import com.example.skhubox.domain.complaint.ComplaintStatus;
import com.example.skhubox.domain.operation.OperationLogType;
import com.example.skhubox.domain.user.User;
import com.example.skhubox.domain.user.UserRole;
import com.example.skhubox.dto.complaint.ComplaintAnswerRequest;
import com.example.skhubox.dto.complaint.ComplaintRequest;
import com.example.skhubox.dto.complaint.ComplaintResponse;
import com.example.skhubox.exception.BusinessException;
import com.example.skhubox.exception.ErrorCode;
import com.example.skhubox.repository.ComplaintRepository;
import com.example.skhubox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final OperationLogService operationLogService;

    @Override
    @Transactional
    public ComplaintResponse createComplaint(String studentNumber, ComplaintRequest request) {
        User user = userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Complaint complaint = Complaint.builder()
                .user(user)
                .lockerNumber(request.getLockerNumber())
                .content(request.getContent())
                .build();

        complaintRepository.save(complaint);
        operationLogService.log(
                OperationLogType.COMPLAINT_SUBMITTED,
                "신규 민원 접수",
                request.getLockerNumber() + "번 사물함 민원이 접수되었습니다."
        );

        // 관리자들에게 알림 생성
        List<User> admins = userRepository.findAllByRole(UserRole.ADMIN);
        for (User admin : admins) {
            notificationService.createNotification(
                    admin,
                    "신규 민원 접수",
                    String.format("%s번 사물함에 새로운 민원이 접수되었습니다.", request.getLockerNumber()),
                    com.example.skhubox.domain.notification.NotificationType.COMPLAINT
            );
        }

        return ComplaintResponse.of(complaint);
    }

    @Override
    public ComplaintResponse getComplaintDetail(String studentNumber, Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPLAINT_NOT_FOUND));
        if (complaint.getUser() == null || !complaint.getUser().getStudentNumber().equals(studentNumber)) {
            throw new BusinessException(ErrorCode.COMPLAINT_ACCESS_DENIED);
        }
        return ComplaintResponse.of(complaint);
    }

    @Override
    public List<ComplaintResponse> getMyComplaints(String studentNumber) {
        User user = userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return complaintRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(ComplaintResponse::of)
                .collect(Collectors.toList());
    }

    @Override
    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAll().stream()
                .map(ComplaintResponse::of)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelComplaint(String studentNumber, Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPLAINT_NOT_FOUND));
        if (complaint.getUser() == null || !complaint.getUser().getStudentNumber().equals(studentNumber)) {
            throw new BusinessException(ErrorCode.COMPLAINT_ACCESS_DENIED);
        }
        if (complaint.getStatus() != ComplaintStatus.PENDING) {
            throw new BusinessException(ErrorCode.COMPLAINT_CANNOT_CANCEL);
        }
        complaint.updateStatus(ComplaintStatus.CANCELLED);
    }

    private static final java.util.Set<ComplaintStatus> ADMIN_ALLOWED_STATUSES = java.util.Set.of(
            ComplaintStatus.UNDER_REVIEW, ComplaintStatus.IN_PROGRESS,
            ComplaintStatus.COMPLETED, ComplaintStatus.REJECTED
    );
    private static final java.util.Set<ComplaintStatus> CLOSED_STATUSES = java.util.Set.of(
            ComplaintStatus.COMPLETED, ComplaintStatus.CANCELLED, ComplaintStatus.REJECTED
    );

    @Override
    @Transactional
    public ComplaintResponse answerComplaint(Long complaintId, ComplaintAnswerRequest request) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPLAINT_NOT_FOUND));

        if (CLOSED_STATUSES.contains(complaint.getStatus())) {
            throw new BusinessException(ErrorCode.COMPLAINT_ALREADY_CLOSED);
        }
        if (!ADMIN_ALLOWED_STATUSES.contains(request.getStatus())) {
            throw new BusinessException(ErrorCode.COMPLAINT_INVALID_STATUS);
        }

        complaint.answerComplaint(request.getStatus(), request.getAnswer());
        if (request.getStatus() == ComplaintStatus.COMPLETED) {
            operationLogService.log(
                    OperationLogType.COMPLAINT_PROCESSED,
                    "민원 처리 완료",
                    complaint.getLockerNumber() + "번 사물함 민원이 처리 완료되었습니다."
            );
        }

        // 탈퇴 사용자의 민원은 user가 null이므로 알림 생략
        if (complaint.getUser() != null) {
            notificationService.createNotification(
                    complaint.getUser(),
                    "민원 답변 등록",
                    String.format("%s번 사물함 민원에 대한 답변이 등록되었습니다.", complaint.getLockerNumber()),
                    com.example.skhubox.domain.notification.NotificationType.COMPLAINT
            );
        }

        return ComplaintResponse.of(complaint);
    }
}
