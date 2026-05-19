package com.example.skhubox.domain.complaint;

import com.example.skhubox.domain.common.BaseEntity;
import com.example.skhubox.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "complaints")
public class Complaint extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(nullable = false)
    private String lockerNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintStatus status;

    @Builder
    public Complaint(User user, String lockerNumber, String content) {
        this.user = user;
        this.lockerNumber = lockerNumber;
        this.content = content;
        this.status = ComplaintStatus.PENDING;
    }

    public void updateStatus(ComplaintStatus status) {
        this.status = status;
    }

    public void answerComplaint(ComplaintStatus status, String answer) {
        this.status = status;
        this.answer = answer;
    }
}
