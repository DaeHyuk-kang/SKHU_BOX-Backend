package com.example.skhubox.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    private String studentNumber;
    private String name;
    private String email;
    private String department;
    private String password;
}
