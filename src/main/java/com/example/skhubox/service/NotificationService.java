package com.example.skhubox.service;

import com.example.skhubox.domain.notification.Notification;
import com.example.skhubox.domain.user.User;
import com.example.skhubox.dto.NotificationResponse;
import com.example.skhubox.exception.BusinessException;
import com.example.skhubox.exception.ErrorCode;
import com.example.skhubox.repository.NotificationRepository;
import com.example.skhubox.repository.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public List<NotificationResponse> getNotifications(String studentNumber) {
        User user = getUserByStudentNumber(studentNumber);
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String studentNumber) {
        User user = getUserByStudentNumber(studentNumber);
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Transactional
    public void markAllAsRead(String studentNumber) {
        User user = getUserByStudentNumber(studentNumber);
        notificationRepository.markAllAsReadByUserId(user.getId());
    }

    @Transactional
    public void markAsRead(String studentNumber, Long notificationId) {
        User user = getUserByStudentNumber(studentNumber);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NOTIFICATION_ACCESS_DENIED);
        }
        notification.markAsRead();
    }

    @Transactional
    public void createNotification(User user, String title, String content, com.example.skhubox.domain.notification.NotificationType type) {
        Notification notification = new Notification(user, title, content, type);
        notificationRepository.save(notification);

        // 실시간 푸시 알림 발송 (FCM 토큰이 있는 경우에만)
        if (user.isNotificationEnabled() && user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
            sendPushNotification(user.getFcmToken(), title, content);
        }
    }

    private void sendPushNotification(String token, String title, String content) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setWebpushConfig(WebpushConfig.builder()
                            .setNotification(new WebpushNotification(title, content))
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().sendAsync(message).get();
            log.info("Successfully sent FCM message: {}", response);
        } catch (Exception e) {
            log.error("Failed to send FCM message: {}", e.getMessage());
        }
    }

    private User getUserByStudentNumber(String studentNumber) {
        return userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
