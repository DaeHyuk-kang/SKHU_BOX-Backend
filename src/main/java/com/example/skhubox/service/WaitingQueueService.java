package com.example.skhubox.service;

import com.example.skhubox.dto.QueueResponse;

public interface WaitingQueueService {
    QueueResponse register(String studentNumber, Long lockerId);
    Long getRank(String studentNumber, Long lockerId);
    String getFirstStudentNumber(Long lockerId);
    boolean isFirstUser(String studentNumber, Long lockerId);
    void removeFromQueue(String studentNumber, Long lockerId);
    void skipFirstUser(Long lockerId);
    void clearAllQueues();
}
