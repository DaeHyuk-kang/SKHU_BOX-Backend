package com.example.skhubox.repository;

import com.example.skhubox.domain.user.User;
import com.example.skhubox.domain.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    default Optional<User> findByStudentNumber(String studentNumber) {
        return findByStudentNumberAndDeletedFalse(studentNumber);
    }

    Optional<User> findByStudentNumberAndDeletedFalse(String studentNumber);

    Optional<User> findByStudentNumberAndEmailAndDeletedFalse(String studentNumber, String email);

    Optional<User> findByEmailAndDeletedFalse(String email);

    default boolean existsByStudentNumber(String studentNumber) {
        return existsByStudentNumberAndDeletedFalse(studentNumber);
    }

    default boolean existsByEmail(String email) {
        return existsByEmailAndDeletedFalse(email);
    }

    boolean existsByStudentNumberAndDeletedFalse(String studentNumber);

    boolean existsByEmailAndDeletedFalse(String email);

    List<User> findAllByRole(UserRole role);

    List<User> findAllByRoleAndDeletedFalse(UserRole role);

    List<User> findAllByDeletedFalseOrderByCreatedAtDesc();

    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
