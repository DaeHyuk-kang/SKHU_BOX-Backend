package com.example.skhubox.service;

import com.example.skhubox.common.RedisKeys;
import com.example.skhubox.dto.QueueResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaitingQueueServiceImpl implements WaitingQueueService {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public QueueResponse register(String studentNumber, Long lockerId) {
        String key = RedisKeys.LOCKER_QUEUE + lockerId;

        Long existingRank = redisTemplate.opsForZSet().rank(key, studentNumber);
        if (existingRank != null) {
            log.info("[Queue] User {} is already in queue for locker {}. Rank: {}", studentNumber, lockerId, existingRank + 1);
            return QueueResponse.of(lockerId, existingRank + 1, "이미 대기열에 등록되어 있습니다.");
        }

        redisTemplate.opsForZSet().add(key, studentNumber, System.currentTimeMillis());
        redisTemplate.expire(key, 10, TimeUnit.MINUTES);

        Long rank = redisTemplate.opsForZSet().rank(key, studentNumber);
        long displayRank = (rank != null) ? rank + 1 : 0;

        log.info("[Queue] User {} registered for locker {}. Assigned Rank: {}", studentNumber, lockerId, displayRank);
        return QueueResponse.of(lockerId, displayRank, "대기열에 성공적으로 등록되었습니다.");
    }

    @Override
    public Long getRank(String studentNumber, Long lockerId) {
        String key = RedisKeys.LOCKER_QUEUE + lockerId;
        Long rank = redisTemplate.opsForZSet().rank(key, studentNumber);
        return (rank != null) ? rank + 1 : null;
    }

    @Override
    public String getFirstStudentNumber(Long lockerId) {
        String key = RedisKeys.LOCKER_QUEUE + lockerId;
        Set<String> members = redisTemplate.opsForZSet().range(key, 0, 0);
        if (members == null || members.isEmpty()) {
            return null;
        }
        return members.iterator().next();
    }

    @Override
    public boolean isFirstUser(String studentNumber, Long lockerId) {
        String first = getFirstStudentNumber(lockerId);
        return studentNumber != null && studentNumber.equals(first);
    }

    @Override
    public void removeFromQueue(String studentNumber, Long lockerId) {
        String key = RedisKeys.LOCKER_QUEUE + lockerId;
        redisTemplate.opsForZSet().remove(key, studentNumber);
        log.info("[Queue] User {} removed from queue for locker {}", studentNumber, lockerId);
    }

    @Override
    public void skipFirstUser(Long lockerId) {
        String key = RedisKeys.LOCKER_QUEUE + lockerId;
        String first = getFirstStudentNumber(lockerId);
        if (first != null) {
            redisTemplate.opsForZSet().removeRange(key, 0, 0);
            log.warn("[Queue-Admin] First user {} skipped for locker {} by administrator", first, lockerId);
        }
    }

    @Override
    public void clearAllQueues() {
        Set<String> keys = redisTemplate.keys(RedisKeys.LOCKER_QUEUE + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("[Queue-Admin] Cleared {} queue keys on queue mode disable", keys.size());
        }
    }
}
