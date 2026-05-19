package com.example.skhubox.service;

import com.example.skhubox.common.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueueTimeoutScheduler {

    private static final long TIMEOUT_MILLIS = 10 * 60 * 1000L;

    private final RedisTemplate<String, String> redisTemplate;
    private final QueueModeSettingService queueModeSettingService;

    @Scheduled(fixedDelay = 60000)
    public void expireTimedOutQueueEntries() {
        if (!queueModeSettingService.isQueueModeEnabled()) return;

        long cutoff = System.currentTimeMillis() - TIMEOUT_MILLIS;

        // active zone(rank 1~500) 중 10분 지난 사용자만 제거
        Set<String> activeZone = redisTemplate.opsForZSet()
                .range(RedisKeys.LOCKER_QUEUE_GLOBAL, 0, 499);

        if (activeZone == null || activeZone.isEmpty()) return;

        for (String studentNumber : activeZone) {
            Double score = redisTemplate.opsForZSet().score(RedisKeys.LOCKER_QUEUE_GLOBAL, studentNumber);
            if (score != null && score < cutoff) {
                redisTemplate.opsForZSet().remove(RedisKeys.LOCKER_QUEUE_GLOBAL, studentNumber);
                redisTemplate.delete(RedisKeys.REFRESH_TOKEN + studentNumber);
                log.info("[Queue-Timeout] User {} removed and logged out (10min timeout)", studentNumber);
            }
        }
    }
}
