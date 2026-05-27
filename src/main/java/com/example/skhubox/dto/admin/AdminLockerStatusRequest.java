package com.example.skhubox.dto.admin;

import com.example.skhubox.domain.locker.LockerStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminLockerStatusRequest {

    @NotNull(message = "변경할 상태는 필수입니다.")
    private LockerStatus status;
}
