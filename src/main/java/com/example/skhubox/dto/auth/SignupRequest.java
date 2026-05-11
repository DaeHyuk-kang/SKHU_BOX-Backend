package com.example.skhubox.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "학번은 필수 입력 사항입니다.")
    @Pattern(regexp = "^[0-9]{9}$", message = "학번은 9자리로 입력해주세요.")
    private String studentNumber;

    @NotBlank(message = "이름은 필수 입력 사항입니다.")
    private String name;

    @NotBlank(message = "이메일은 필수 입력 사항입니다.")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@office\\.skhu\\.ac\\.kr$", message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "학부는 필수 입력 사항입니다.")
    @Pattern(regexp = "^(인문융합콘텐츠학부|경영학부|사회융합학부|미디어콘텐츠융합학부|미래융합학부|소프트웨어융합학부|국제학부|인문융합자율학부|사회융합자율학부|미디어콘텐츠융합자율학부|IT융합자율학부)$", message = "올바른 학부명이 아닙니다.")
    private String department;

    @NotBlank(message = "비밀번호는 필수 입력 사항입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;
}
