package com.example.skhubox.domain.user;

import com.example.skhubox.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String studentNumber;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 50)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    private String fcmToken;

    @Column(nullable = false)
    private boolean notificationEnabled = true;

    @Column(nullable = false)
    private boolean deleted = false;

    public User(String studentNumber, String name, String email, String department, String password) {
        this.studentNumber = studentNumber;
        this.name = name;
        this.email = email;
        this.department = department;
        this.password = password;
        this.role = UserRole.USER;
    }

    public void changeRole(UserRole role) {
        this.role = role;
    }

    public void assignAdminRole() {
        this.role = UserRole.ADMIN;
    }

    public void assignUserRole() {
        this.role = UserRole.USER;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void updateNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    public void updateProfile(String name, String department) {
        this.name = name;
        this.department = department;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
