package com.example.skhubox.service;

import com.example.skhubox.common.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaitingQueueServiceImpl implements WaitingQueueService {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Long register(String studentNumber) {
        Long existingRank = redisTemplate.opsForZSet().rank(RedisKeys.LOCKER_QUEUE_GLOBAL, studentNumber);
        if (existingRank != null) {
            log.info("[Queue] User {} already in queue. Rank: {}", studentNumber, existingRank + 1);
            return existingRank + 1;
        }
        redisTemplate.opsForZSet().add(RedisKeys.LOCKER_QUEUE_GLOBAL, studentNumber, System.currentTimeMillis());
        Long rank = redisTemplate.opsForZSet().rank(RedisKeys.LOCKER_QUEUE_GLOBAL, studentNumber);
        long displayRank = (rank != null) ? rank + 1 : 0;
        log.info("[Queue] User {} registered. Rank: {}", studentNumber, displayRank);
        return displayRank;
    }

    @Override
    public Long getRank(String studentNumber) {
        Long rank = redisTemplate.opsForZSet().rank(RedisKeys.LOCKER_QUEUE_GLOBAL, studentNumber);
        return (rank != null) ? rank + 1 : null;
    }

    @Override
    public boolean isFirstUser(String studentNumber) {
        Set<String> members = redisTemplate.opsForZSet().range(RedisKeys.LOCKER_QUEUE_GLOBAL, 0, 0);
        if (members == null || members.isEmpty()) return false;
        return studentNumber.equals(members.iterator().next());
    }

    @Override
    public void removeFromQueue(String studentNumber) {
        redisTemplate.opsForZSet().remove(RedisKeys.LOCKER_QUEUE_GLOBAL, studentNumber);
        log.info("[Queue] User {} removed from global queue", studentNumber);
    }

    @Override
    public void skipFirstUser() {
        Set<String> members = redisTemplate.opsForZSet().range(RedisKeys.LOCKER_QUEUE_GLOBAL, 0, 0);
        if (members == null || members.isEmpty()) return;
        String first = members.iterator().next();
        redisTemplate.opsForZSet().removeRange(RedisKeys.LOCKER_QUEUE_GLOBAL, 0, 0);
        log.warn("[Queue] First user {} skipped by administrator", first);
    }

    @Override
    public void clearAllQueues() {
        redisTemplate.delete(RedisKeys.LOCKER_QUEUE_GLOBAL);
        log.info("[Queue] Global queue cleared");
    }

    @Override
    public void removeFromAllQueues(String studentNumber) {
        redisTemplate.opsForZSet().remove(RedisKeys.LOCKER_QUEUE_GLOBAL, studentNumber);
        log.info("[Queue] User {} removed from global queue on withdraw", studentNumber);
    }

    @Override
    public long getQueueSize() {
        Long size = redisTemplate.opsForZSet().size(RedisKeys.LOCKER_QUEUE_GLOBAL);
        return size != null ? size : 0;
    }

    @Override
    public String getFirstStudentNumber() {
        Set<String> members = redisTemplate.opsForZSet().range(RedisKeys.LOCKER_QUEUE_GLOBAL, 0, 0);
        if (members == null || members.isEmpty()) return null;
        return members.iterator().next();
    }
}
