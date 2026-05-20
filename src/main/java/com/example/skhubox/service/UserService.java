package com.example.skhubox.service;

import com.example.skhubox.domain.operation.OperationLogType;
import com.example.skhubox.domain.user.AdminActionLog;
import com.example.skhubox.domain.user.User;
import com.example.skhubox.domain.user.UserRole;
import com.example.skhubox.common.RedisKeys;
import com.example.skhubox.dto.ChangePasswordRequest;
import com.example.skhubox.dto.NotificationSettingResponse;
import com.example.skhubox.dto.UpdateProfileRequest;
import com.example.skhubox.dto.UserInfoResponse;
import com.example.skhubox.exception.BusinessException;
import com.example.skhubox.exception.ErrorCode;
import com.example.skhubox.repository.AdminActionLogRepository;
import com.example.skhubox.repository.ComplaintRepository;
import com.example.skhubox.repository.LockerReservationRepository;
import com.example.skhubox.repository.NotificationRepository;
import com.example.skhubox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final AdminActionLogRepository adminActionLogRepository;
    private final LockerReservationService lockerReservationService;
    private final WaitingQueueService waitingQueueService;
    private final NotificationRepository notificationRepository;
    private final ComplaintRepository complaintRepository;
    private final LockerReservationRepository lockerReservationRepository;
    private final OperationLogService operationLogService;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;

    public User findByStudentNumber(String studentNumber) {
        return userRepository.findByStudentNumberAndDeletedFalse(studentNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void updateUserRole(String adminStudentNumber, String targetStudentNumber, UserRole newRole) {
        User targetUser = findByStudentNumber(targetStudentNumber);

        String actionType = newRole == UserRole.ADMIN ? "ROLE_UPGRADE" : "ROLE_DOWNGRADE";
        String details = String.format("Role changed from %s to %s", targetUser.getRole(), newRole);

        if (newRole == UserRole.ADMIN) {
            targetUser.assignAdminRole();
        } else {
            targetUser.assignUserRole();
        }

        // 로그 저장
        AdminActionLog log = new AdminActionLog(adminStudentNumber, targetStudentNumber, actionType, details);
        adminActionLogRepository.save(log);
    }

    public boolean existsByStudentNumber(String studentNumber) {
        return userRepository.existsByStudentNumberAndDeletedFalse(studentNumber);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailAndDeletedFalse(email);
    }

    @Transactional
    public void updateFcmToken(String studentNumber, String token) {
        User user = findByStudentNumber(studentNumber);
        user.updateFcmToken(token);
    }

    @Transactional
    public NotificationSettingResponse updateNotificationSetting(String studentNumber, boolean enabled) {
        User user = findByStudentNumber(studentNumber);
        user.updateNotificationEnabled(enabled);
        return new NotificationSettingResponse(user.isNotificationEnabled());
    }

    @Transactional
    public UserInfoResponse updateProfile(String studentNumber, UpdateProfileRequest request) {
        User user = findByStudentNumber(studentNumber);
        user.updateProfile(request.getName(), request.getDepartment());
        return UserInfoResponse.from(user);
    }

    @Transactional
    public void changePassword(String studentNumber, ChangePasswordRequest request) {
        User user = findByStudentNumber(studentNumber);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    public UserInfoResponse getUserInfo(String studentNumber) {
        return UserInfoResponse.from(findByStudentNumber(studentNumber));
    }

    @Transactional
    public void adminWithdrawUser(String adminStudentNumber, String targetStudentNumber) {
        if (adminStudentNumber.equals(targetStudentNumber)) {
            throw new BusinessException(ErrorCode.CANNOT_WITHDRAW_SELF);
        }
        User target = findByStudentNumber(targetStudentNumber);
        if (target.getRole() == UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.CANNOT_WITHDRAW_ADMIN);
        }
        doWithdraw(target);
    }

    @Transactional
    public void withdrawUser(String studentNumber) {
        User user = findByStudentNumber(studentNumber);
        doWithdraw(user);
    }

    private void doWithdraw(User user) {
        String studentNumber = user.getStudentNumber();
        Long userId = user.getId();

        // 1. 사용 중인 사물함 반납
        try {
            lockerReservationService.returnLocker(studentNumber);
        } catch (BusinessException e) {
            if (e.getErrorCode() != ErrorCode.NO_ACTIVE_RESERVATION) {
                throw e;
            }
        }

        // 2. 대기열에서 제거
        waitingQueueService.removeFromAllQueues(studentNumber);

        // 3. Refresh Token 삭제 (즉시 로그아웃)
        redisTemplate.delete(RedisKeys.REFRESH_TOKEN + studentNumber);

        // 4. 알림 삭제 (개인 데이터)
        notificationRepository.deleteAllByUserId(userId);

        // 5. 민원 user 참조 NULL 처리 (민원 기록 보존)
        complaintRepository.nullifyUser(userId);

        // 6. 예약 이력 user 참조 NULL 처리 (이력 보존)
        lockerReservationRepository.nullifyUser(userId);

        // 7. 로그 기록 후 사용자 물리적 삭제
        operationLogService.log(OperationLogType.USER_WITHDRAWN, "회원 탈퇴", studentNumber);
        userRepository.delete(user);
    }
}
