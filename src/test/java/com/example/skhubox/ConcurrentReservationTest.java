package com.example.skhubox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 600명 동시 사물함 예약 테스트 (각 유저가 서로 다른 사물함 시도)
 * 전제: 대기열 모드 ON, 테스트 사용자 600명 사전 가입, NORMAL 사물함 충분히 존재
 */
public class ConcurrentReservationTest {

    private static final String BASE_URL = "http://43.201.114.134";
    private static final int TOTAL_USERS = 600;
    private static final int ACTIVE_ZONE_LIMIT = 500;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final List<String> tokens = new CopyOnWriteArrayList<>();
    private static final List<Long> lockerIds = new CopyOnWriteArrayList<>();

    @BeforeAll
    static void setUp() throws Exception {
        // 로그인
        System.out.println("=== 테스트 사용자 600명 로그인 중... ===");
        ExecutorService loginPool = Executors.newFixedThreadPool(50);
        List<Future<String>> futures = new ArrayList<>();
        for (int i = 1; i <= TOTAL_USERS; i++) {
            final int idx = i;
            futures.add(loginPool.submit(() -> login("test" + String.format("%04d", idx), "Test1234!")));
        }
        for (Future<String> f : futures) {
            try {
                String token = f.get(10, TimeUnit.SECONDS);
                if (token != null) tokens.add(token);
            } catch (Exception ignored) {}
        }
        loginPool.shutdown();
        System.out.println("로그인 성공: " + tokens.size() + "명");

        // 사물함 목록 조회
        System.out.println("=== 사물함 목록 조회 중... ===");
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/lockers"))
                .GET().build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode data = mapper.readTree(res.body()).path("data");
        for (JsonNode locker : data) {
            if ("NORMAL".equals(locker.path("status").asText())) {
                lockerIds.add(locker.path("id").asLong());
            }
        }
        System.out.println("예약 가능한 사물함: " + lockerIds.size() + "개");
    }

    @Test
    void concurrent600UsersDifferentLockersTest() throws Exception {
        System.out.println("\n=== 600명 동시 예약 접근 시작 (각자 다른 사물함) ===");

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger waitingZone = new AtomicInteger(0);
        AtomicInteger alreadyReserved = new AtomicInteger(0);
        AtomicInteger noLocker = new AtomicInteger(0);
        AtomicInteger other = new AtomicInteger(0);

        CountDownLatch ready = new CountDownLatch(tokens.size());
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(tokens.size());

        for (int i = 0; i < tokens.size(); i++) {
            final String token = tokens.get(i);
            // 사물함이 부족하면 마지막 사물함 재사용 (round-robin)
            final long lockerId = lockerIds.get(i % lockerIds.size());

            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    HttpResponse<String> resp = reserveLockerResponse(token, lockerId);
                    if (resp == null) { other.incrementAndGet(); return; }
                    int status = resp.statusCode();
                    JsonNode json = mapper.readTree(resp.body());
                    String code = json.path("code").asText();
                    String message = json.path("message").asText();

                    if (status == 200) {
                        success.incrementAndGet();
                    } else if ("L007".equals(code) && message.contains("대기 중")) {
                        waitingZone.incrementAndGet();
                    } else if ("L003".equals(code) || "L009".equals(code)) {
                        alreadyReserved.incrementAndGet();
                    } else if ("L004".equals(code)) {
                        // 이미 다른 사물함을 예약한 사용자
                        alreadyReserved.incrementAndGet();
                    } else if ("L001".equals(code) || "L002".equals(code)) {
                        noLocker.incrementAndGet();
                    } else {
                        other.incrementAndGet();
                        System.out.println("기타: " + status + " / " + code + " / " + message);
                    }
                } catch (Exception e) {
                    other.incrementAndGet();
                }
            });
        }

        ready.await();
        long startTime = System.currentTimeMillis();
        start.countDown();

        pool.shutdown();
        pool.awaitTermination(60, TimeUnit.SECONDS);
        long elapsed = System.currentTimeMillis() - startTime;

        System.out.println("\n=== 결과 ===");
        System.out.println("총 요청:            " + tokens.size() + "명");
        System.out.println("예약 성공:          " + success.get() + "명");
        System.out.println("대기열 대기:         " + waitingZone.get() + "명 (rank 501~600)");
        System.out.println("이미예약됨/충돌:     " + alreadyReserved.get() + "명 (L003/L004/L009)");
        System.out.println("사물함 없음/불가:    " + noLocker.get() + "명");
        System.out.println("기타:               " + other.get() + "명");
        System.out.println("소요시간:           " + elapsed + "ms");
        System.out.println("합계:               " + (success.get() + waitingZone.get() + alreadyReserved.get() + noLocker.get() + other.get()));
        System.out.println("\n예약 가능 사물함: " + lockerIds.size() + "개 / 활성존 허용: " + ACTIVE_ZONE_LIMIT + "명");
        System.out.println("이론상 최대 성공: " + Math.min(lockerIds.size(), ACTIVE_ZONE_LIMIT) + "명");

        assert waitingZone.get() <= TOTAL_USERS - ACTIVE_ZONE_LIMIT
                : "대기열 초과: " + waitingZone.get();
    }

    @AfterAll
    static void tearDown() throws Exception {
        // 1. 사물함 반납
        System.out.println("\n=== 테스트 사물함 반납 중... ===");
        ExecutorService pool = Executors.newFixedThreadPool(50);
        AtomicInteger returned = new AtomicInteger(0);

        for (String token : tokens) {
            pool.submit(() -> {
                try {
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + "/api/lockers/return"))
                            .header("Content-Type", "application/json")
                            .header("Authorization", "Bearer " + token)
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .timeout(Duration.ofSeconds(10))
                            .build();
                    HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
                    if (res.statusCode() == 200) returned.incrementAndGet();
                } catch (Exception ignored) {}
            });
        }

        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
        System.out.println("반납 완료: " + returned.get() + "명");

        // 2. 테스트 유저 관련 DB 레코드 정리 (누적 방지)
        System.out.println("=== 테스트 데이터 정리 중... ===");
        try {
            HttpRequest cleanupReq = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/admin/test-cleanup"))
                    .header("Content-Type", "application/json")
                    .DELETE()
                    .timeout(Duration.ofSeconds(15))
                    .build();
            client.send(cleanupReq, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ignored) {}
        System.out.println("정리 완료");
    }

    private static String login(String studentNumber, String password) {
        try {
            String body = String.format("{\"studentNumber\":\"%s\",\"password\":\"%s\"}", studentNumber, password);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode json = mapper.readTree(res.body());
            return json.path("data").path("accessToken").asText(null);
        } catch (Exception e) {
            return null;
        }
    }

    private static HttpResponse<String> reserveLockerResponse(String token, Long lockerId) {
        try {
            return client.send(buildReserveRequest(token, lockerId), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            return null;
        }
    }

    private static HttpRequest buildReserveRequest(String token, Long lockerId) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/lockers/reserve"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString("{\"lockerId\":" + lockerId + "}"))
                .timeout(Duration.ofSeconds(30))
                .build();
    }
}
