package com.example.spacehub.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.spacehub.domain.locker.Locker;
import com.example.spacehub.domain.locker.LockerStatus;
import com.example.spacehub.domain.reservation.LockerReservation;
import com.example.spacehub.domain.reservation.ReservationStatus;
import com.example.spacehub.domain.user.User;
import com.example.spacehub.dto.LockerReservationResponse;
import com.example.spacehub.repository.LockerReservationRepository;
import com.example.spacehub.repository.LockerRepository;
import com.example.spacehub.repository.UserRepository;

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

    @Test
    @DisplayName("사물함 예약 성공")
    void reserveLocker_success() {
        User user = userRepository.save(new User("20210001", "홍길동", "hong1@example.com"));
        Locker locker = lockerRepository.save(new Locker("A-101", "공학관 1층"));

        LockerReservationResponse response =
                lockerReservationService.reserveLocker(user.getId(), locker.getId());

        Long reservationId = response.getReservationId();

        LockerReservation reservation = lockerReservationRepository.findById(reservationId).orElseThrow();

        assertNotNull(reservationId);
        assertEquals(ReservationStatus.ACTIVE, reservation.getStatus());
        assertEquals(user.getId(), reservation.getUser().getId());
        assertEquals(locker.getId(), reservation.getLocker().getId());

        Locker savedLocker = lockerRepository.findById(locker.getId()).orElseThrow();
        assertEquals(LockerStatus.RESERVED, savedLocker.getStatus());
    }

    @Test
    @DisplayName("사용자는 동시에 하나의 사물함만 예약 가능")
    void reserveLocker_fail_when_user_already_has_active_reservation() {
        User user = userRepository.save(new User("20210002", "김철수", "kim1@example.com"));
        Locker locker1 = lockerRepository.save(new Locker("A-102", "공학관 1층"));
        Locker locker2 = lockerRepository.save(new Locker("A-103", "공학관 1층"));

        lockerReservationService.reserveLocker(user.getId(), locker1.getId());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> lockerReservationService.reserveLocker(user.getId(), locker2.getId()));

        assertEquals("이미 예약 중인 사물함이 있습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("이미 예약된 사물함은 다른 사용자가 예약할 수 없다")
    void reserveLocker_fail_when_locker_already_reserved() {
        User user1 = userRepository.save(new User("20210003", "이영희", "lee1@example.com"));
        User user2 = userRepository.save(new User("20210004", "박민수", "park1@example.com"));
        Locker locker = lockerRepository.save(new Locker("A-104", "공학관 1층"));

        lockerReservationService.reserveLocker(user1.getId(), locker.getId());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> lockerReservationService.reserveLocker(user2.getId(), locker.getId()));

        assertTrue(
                exception.getMessage().equals("이미 예약된 사물함입니다.")
                        || exception.getMessage().equals("이미 사용 중인 사물함입니다.")
        );
    }

    @Test
    @DisplayName("사물함 반납 성공")
    void returnLocker_success() {
        User user = userRepository.save(new User("20210005", "최유진", "choi1@example.com"));
        Locker locker = lockerRepository.save(new Locker("A-105", "공학관 1층"));

        LockerReservationResponse response =
                lockerReservationService.reserveLocker(user.getId(), locker.getId());

        Long reservationId = response.getReservationId();

        lockerReservationService.returnLocker(user.getId());

        LockerReservation reservation = lockerReservationRepository.findById(reservationId).orElseThrow();
        Locker savedLocker = lockerRepository.findById(locker.getId()).orElseThrow();

        assertEquals(ReservationStatus.RETURNED, reservation.getStatus());
        assertNotNull(reservation.getReturnedAt());
        assertEquals(LockerStatus.AVAILABLE, savedLocker.getStatus());
    }

    @Test
    @DisplayName("사용 중인 사물함을 다른 사용 가능한 사물함으로 변경 성공")
    void changeLocker_success() {
        User user = userRepository.save(new User("20210006", "정하늘", "jung1@example.com"));
        Locker oldLocker = lockerRepository.save(new Locker("A-106", "공학관 1층"));
        Locker newLocker = lockerRepository.save(new Locker("A-107", "공학관 1층"));

        LockerReservationResponse oldResponse =
                lockerReservationService.reserveLocker(user.getId(), oldLocker.getId());

        LockerReservationResponse newResponse =
                lockerReservationService.changeLocker(user.getId(), newLocker.getId());

        Long oldReservationId = oldResponse.getReservationId();
        Long newReservationId = newResponse.getReservationId();

        LockerReservation oldReservation = lockerReservationRepository.findById(oldReservationId).orElseThrow();
        LockerReservation newReservation = lockerReservationRepository.findById(newReservationId).orElseThrow();

        Locker savedOldLocker = lockerRepository.findById(oldLocker.getId()).orElseThrow();
        Locker savedNewLocker = lockerRepository.findById(newLocker.getId()).orElseThrow();

        assertEquals(ReservationStatus.RETURNED, oldReservation.getStatus());
        assertEquals(ReservationStatus.ACTIVE, newReservation.getStatus());

        assertEquals(LockerStatus.AVAILABLE, savedOldLocker.getStatus());
        assertEquals(LockerStatus.RESERVED, savedNewLocker.getStatus());
    }

    @Test
    @DisplayName("동시에 같은 사물함 예약 요청이 들어오면 1명만 성공해야 한다")
    void reserveLocker_concurrent_onlyOneSuccess() throws InterruptedException {
        User user1 = userRepository.save(new User("20210007", "사용자1", "user1@example.com"));
        User user2 = userRepository.save(new User("20210008", "사용자2", "user2@example.com"));
        Locker locker = lockerRepository.save(new Locker("A-108", "공학관 1층"));

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        List<Long> successResults = new ArrayList<>();
        List<Exception> failResults = new ArrayList<>();

        Runnable task1 = () -> {
            try {
                LockerReservationResponse response =
                        lockerReservationService.reserveLocker(user1.getId(), locker.getId());
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
                        lockerReservationService.reserveLocker(user2.getId(), locker.getId());
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

        Locker savedLocker = lockerRepository.findById(locker.getId()).orElseThrow();
        assertEquals(LockerStatus.RESERVED, savedLocker.getStatus());
    }
}

