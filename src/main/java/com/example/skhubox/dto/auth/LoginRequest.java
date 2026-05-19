package com.example.skhubox.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank
    private String studentNumber;
    @NotBlank
    private String password;
}
