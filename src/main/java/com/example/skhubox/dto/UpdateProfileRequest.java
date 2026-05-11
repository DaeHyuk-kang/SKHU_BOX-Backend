package com.example.skhubox.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "이름은 필수 입력 사항입니다.")
    private String name;

    @Pattern(
        regexp = "^(인문융합콘텐츠학부|경영학부|사회융합학부|미디어콘텐츠융합학부|미래융합학부|소프트웨어융합학부|국제학부|인문융합자율학부|사회융합자율학부|미디어콘텐츠융합자율학부|IT융합자율학부)?$",
        message = "올바른 학부명이 아닙니다."
    )
    private String department;
}
