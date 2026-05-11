package com.example.skhubox.domain.complaint;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ComplaintStatus {
    PENDING("대기중"),
    UNDER_REVIEW("확인중"),
    IN_PROGRESS("처리중"),
    COMPLETED("완료"),
    CANCELLED("취소됨"),
    REJECTED("반려됨");

    private final String description;
}
