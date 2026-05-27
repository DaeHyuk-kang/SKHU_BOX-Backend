package com.example.skhubox.controller.admin;

import com.example.skhubox.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Test Cleanup API", description = "테스트 데이터 정리 (운영 환경에서는 비활성화)")
@RestController
@RequestMapping("/api/admin")
@Slf4j
@Profile("!prod")
public class AdminTestCleanupController {

    @PersistenceContext
    private EntityManager em;

    @Operation(summary = "테스트 유저 관련 데이터 정리",
            description = "student_number가 'test'로 시작하는 유저의 예약내역, 알림, 운영로그를 삭제합니다.")
    @DeleteMapping("/test-cleanup")
    @Transactional
    public ResponseEntity<ApiResponse<String>> cleanupTestData() {
        int notifications = em.createQuery(
                "DELETE FROM Notification n WHERE n.user IS NOT NULL AND n.user.studentNumber LIKE 'test%'")
                .executeUpdate();

        int operationLogs = em.createQuery(
                "DELETE FROM OperationLog o WHERE o.description LIKE '%test0%'")
                .executeUpdate();

        int reservations = em.createQuery(
                "DELETE FROM LockerReservation r WHERE r.user IS NOT NULL AND r.user.studentNumber LIKE 'test%'")
                .executeUpdate();

        // 사물함 상태 NORMAL로 복구 (테스트 유저로만 예약된 사물함)
        em.createQuery(
                "UPDATE Locker l SET l.status = com.example.skhubox.domain.locker.LockerStatus.NORMAL, l.expiredAt = null " +
                "WHERE l.status = com.example.skhubox.domain.locker.LockerStatus.ACTIVE AND NOT EXISTS (" +
                "  SELECT r FROM LockerReservation r WHERE r.locker = l AND r.status = com.example.skhubox.domain.reservation.ReservationStatus.ACTIVE" +
                ")")
                .executeUpdate();

        String msg = String.format("알림 %d건, 운영로그 %d건, 예약내역 %d건 삭제", notifications, operationLogs, reservations);
        log.info("[TestCleanup] {}", msg);
        return ResponseEntity.ok(ApiResponse.ok("테스트 데이터 정리 완료", msg));
    }
}
