package com.example.skhubox.service;

public interface WaitingQueueService {
    Long register(String studentNumber);
    Long getRank(String studentNumber);
    boolean isFirstUser(String studentNumber);
    void removeFromQueue(String studentNumber);
    void skipFirstUser();
    void clearAllQueues();
    void removeFromAllQueues(String studentNumber);
    long getQueueSize();
    String getFirstStudentNumber();
}
