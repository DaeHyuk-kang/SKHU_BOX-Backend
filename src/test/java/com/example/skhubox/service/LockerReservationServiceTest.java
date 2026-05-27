package com.example.skhubox.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.skhubox.domain.locker.Locker;
import com.example.skhubox.domain.locker.LockerStatus;
import com.example.skhubox.domain.reservation.LockerReservation;
import com.example.skhubox.domain.reservation.ReservationStatus;
import com.example.skhubox.domain.user.User;
import com.example.skhubox.dto.LockerReservationResponse;
import com.example.skhubox.exception.BusinessException;
import com.example.skhubox.repository.LockerReservationRepository;
import com.example.skhubox.repository.LockerRepository;
import com.example.skhubox.repository.NotificationRepository;
import com.example.skhubox.repository.OperationLogRepository;
import com.example.skhubox.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LockerReservationServiceTest {

    @Autowired
    private LockerReservationService lockerReservationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LockerRepository lockerRepository;

    @Autowired
    private LockerReservationRepository lockerReservationRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private OperationLogRepository operationLogRepository;

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAllInBatch();
        operationLogRepository.deleteAllInBatch();
        lockerReservationRepository.deleteAllInBatch();
        lockerRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("사물함 예약 성공")
    void reserveLocker_success() {
        User user = userRepository.save(
                new User("202100011", "홍길동", "hong1@office.skhu.ac.kr", "IT융합자율학부","1234")
        );
        Locker locker = lockerRepository.save(
                new Locker("이천환기념관", 1, "남자화장실 옆", "A-101")
        );

        LockerReservationResponse response =
                lockerReservationService.reserveLocker(user.getStudentNumber(), locker.getId());

        Long reservationId = response.getReservationId();

        LockerReservation reservation = lockerReservationRepository.findById(reservationId).orElseThrow();

        assertNotNull(reservationId);
        assertEquals(ReservationStatus.ACTIVE, reservation.getStatus());
        assertEquals(user.getId(), reservation.getUser().getId());
        assertEquals(locker.getId(), reservation.getLocker().getId());

        Locker savedLocker = lockerRepository.findById(locker.getId()).orElseThrow();
        assertEquals(LockerStatus.ACTIVE, savedLocker.getStatus());
        assertTrue(lockerReservationRepository.existsByLocker_IdAndStatus(locker.getId(), ReservationStatus.ACTIVE));
    }

    @Test
    @DisplayName("사용자는 동시에 하나의 사물함만 예약 가능")
    void reserveLocker_fail_when_user_already_has_active_reservation() {
        User user = userRepository.save(
                new User("202100022", "김철수", "kim1@office.skhu.ac.kr", "사회융합자율학부","1234")
        );
        Locker locker1 = lockerRepository.save(
                new Locker("이천환기념관", 1, "남자화장실 옆", "A-102")
        );
        Locker locker2 = lockerRepository.save(
                new Locker("이천환기념관", 1, "남자화장실 옆", "A-103")
        );

        lockerReservationService.reserveLocker(user.getStudentNumber(), locker1.getId());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> lockerReservationService.reserveLocker(user.getStudentNumber(), locker2.getId())
        );

        assertEquals("이미 이용 중인 사물함이 있습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("이미 예약된 사물함은 다른 사용자가 예약할 수 없다")
    void reserveLocker_fail_when_locker_already_reserved() {
        User user1 = userRepository.save(
                new User("202114034", "강대혁", "kangdh0430@office.skhu.ac.kr", "미디어컨텐츠융합자율학부","kang0430")
        );
        User user2 = userRepository.save(
                new User("202100044", "박민수", "park1@office.skhu.ac.kr", "사회융합자율학부","2345")
        );
        Locker locker = lockerRepository.save(
                new Locker("이천환기념관", 1, "남자화장실 옆", "A-104")
        );

        lockerReservationService.reserveLocker(user1.getStudentNumber(), locker.getId());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> lockerReservationService.reserveLocker(user2.getStudentNumber(), locker.getId())
        );

        assertEquals("이미 다른 사용자가 이용 중인 사물함입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("사물함 반납 성공")
    void returnLocker_success() {
        User user = userRepository.save(
                new User("202100055", "최유진", "choi1@office.skhu.ac.kr", "IT융합자율학부","1234")
        );
        Locker locker = lockerRepository.save(
                new Locker("이천환기념관", 1, "남자화장실 옆", "A-105")
        );

        LockerReservationResponse response =
                lockerReservationService.reserveLocker(user.getStudentNumber(), locker.getId());

        Long reservationId = response.getReservationId();

        lockerReservationService.returnLocker(user.getStudentNumber());

        LockerReservation reservation = lockerReservationRepository.findById(reservationId).orElseThrow();
        Locker savedLocker = lockerRepository.findById(locker.getId()).orElseThrow();

        assertEquals(ReservationStatus.RETURNED, reservation.getStatus());
        assertEquals(LockerStatus.NORMAL, savedLocker.getStatus());
        assertFalse(lockerReservationRepository.existsByLocker_IdAndStatus(locker.getId(), ReservationStatus.ACTIVE));
    }

    @Test
    @DisplayName("사용 중인 사물함을 다른 사용 가능한 사물함으로 변경 성공")
    void changeLocker_success() {
        User user = userRepository.save(
                new User("202100066", "정하늘", "jung1@office.skhu.ac.kr", "사회융합자율학부","1234")
        );
        Locker oldLocker = lockerRepository.save(
                new Locker("이천환기념관", 1, "남자화장실 옆", "A-106")
        );
        Locker newLocker = lockerRepository.save(
                new Locker("이천환기념관", 1, "남자화장실 옆", "A-107")
        );

        lockerReservationService.reserveLocker(user.getStudentNumber(), oldLocker.getId());
        lockerReservationService.changeLocker(user.getStudentNumber(), newLocker.getId());

        assertFalse(lockerReservationRepository.existsByLocker_IdAndStatus(oldLocker.getId(), ReservationStatus.ACTIVE));
        assertTrue(lockerReservationRepository.existsByLocker_IdAndStatus(newLocker.getId(), ReservationStatus.ACTIVE));
    }

    @Test
    @DisplayName("동시에 같은 사물함 예약 요청이 들어오면 1명만 성공해야 한다")
    void reserveLocker_concurrent_onlyOneSuccess() throws InterruptedException {
        User user1 = userRepository.save(
                new User("202100077", "사용자1", "user1@office.skhu.ac.kr", "미디어컨텐츠융합자율학부","1234")
        );
        User user2 = userRepository.save(
                new User("202100088", "사용자2", "user2@office.skhu.ac.kr", "미래융합학부","1234")
        );
         Locker locker = lockerRepository.save(
                new Locker("이천환기념관", 1, "남자화장실 옆", "A-108")
        );

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        List<Long> successResults = new ArrayList<>();
        List<Exception> failResults = new ArrayList<>();

        Runnable task1 = () -> {
            try {
                LockerReservationResponse response =
                        lockerReservationService.reserveLocker(user1.getStudentNumber(), locker.getId());
                synchronized (successResults) {
                    successResults.add(response.getReservationId());
                }
            } catch (Exception e) {
                synchronized (failResults) {
                    failResults.add(e);
                }
            } finally {
                countDownLatch.countDown();
            }
        };

        Runnable task2 = () -> {
            try {
                LockerReservationResponse response =
                        lockerReservationService.reserveLocker(user2.getStudentNumber(), locker.getId());
                synchronized (successResults) {
                    successResults.add(response.getReservationId());
                }
            } catch (Exception e) {
                synchronized (failResults) {
                    failResults.add(e);
                }
            } finally {
                countDownLatch.countDown();
            }
        };

        executorService.submit(task1);
        executorService.submit(task2);

        countDownLatch.await();
        executorService.shutdown();

        assertEquals(1, successResults.size(), "예약 성공은 1건이어야 한다.");
        assertEquals(1, failResults.size(), "예약 실패는 1건이어야 한다.");

        long activeReservationCount = lockerReservationRepository.countByLocker_IdAndStatus(
                locker.getId(), ReservationStatus.ACTIVE
        );

        assertEquals(1, activeReservationCount, "해당 사물함의 활성 예약은 1건만 존재해야 한다.");
    }
}
