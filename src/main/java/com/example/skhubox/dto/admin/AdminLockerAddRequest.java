package com.example.skhubox.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminLockerAddRequest {

    @NotBlank(message = "건물명은 필수입니다.")
    private String building;

    @NotNull(message = "층은 필수입니다.")
    @Positive(message = "층은 양수여야 합니다.")
    private Integer floor;

    @NotBlank(message = "위치 상세는 필수입니다.")
    private String locationDetail;

    @NotBlank(message = "사물함 번호는 필수입니다.")
    private String lockerNumber;
}
