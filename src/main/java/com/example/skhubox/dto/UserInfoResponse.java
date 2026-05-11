package com.example.skhubox.dto;

import com.example.skhubox.domain.user.User;
import lombok.Getter;

@Getter
public class UserInfoResponse {

    private final String name;
    private final String studentNumber;
    private final String department;
    private final String email;
    private final String createdAt;

    private UserInfoResponse(User user) {
        this.name = user.getName();
        this.studentNumber = user.getStudentNumber();
        this.department = user.getDepartment();
        this.email = user.getEmail();
        this.createdAt = user.getCreatedAt().toLocalDate().toString();
    }

    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(user);
    }
}
