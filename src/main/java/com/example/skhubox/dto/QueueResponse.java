package com.example.skhubox.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueResponse {
    private Long rank;
    private String message;

    public static QueueResponse of(Long rank, String message) {
        return QueueResponse.builder()
                .rank(rank)
                .message(message)
                .build();
    }
}
