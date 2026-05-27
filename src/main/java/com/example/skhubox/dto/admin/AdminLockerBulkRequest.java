package com.example.skhubox.dto.admin;

import com.example.skhubox.domain.locker.LockerStatus;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AdminLockerBulkRequest {

    @NotEmpty(message = "사물함 ID 목록은 필수입니다.")
    private List<Long> lockerIds;

    private LockerStatus status;
}
