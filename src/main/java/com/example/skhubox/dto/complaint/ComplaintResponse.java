package com.example.skhubox.dto.complaint;

import com.example.skhubox.domain.complaint.Complaint;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ComplaintResponse {
    private Long id;
    private String studentNumber;
    private String lockerNumber;
    private String content;
    private String answer;
    private String status;
    private LocalDateTime createdAt;

    public static ComplaintResponse of(Complaint complaint) {
        return ComplaintResponse.builder()
                .id(complaint.getId())
                .studentNumber(complaint.getUser() != null ? complaint.getUser().getStudentNumber() : null)
                .lockerNumber(complaint.getLockerNumber())
                .content(complaint.getContent())
                .answer(complaint.getAnswer())
                .status(complaint.getStatus().getDescription())
                .createdAt(complaint.getCreatedAt())
                .build();
    }
}
