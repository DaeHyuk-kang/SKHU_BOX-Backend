package com.example.skhubox.service;

import com.example.skhubox.domain.complaint.Complaint;
import com.example.skhubox.domain.complaint.ComplaintStatus;
import com.example.skhubox.domain.user.User;
import com.example.skhubox.dto.complaint.ComplaintAnswerRequest;
import com.example.skhubox.dto.complaint.ComplaintRequest;
import com.example.skhubox.dto.complaint.ComplaintResponse;
import com.example.skhubox.repository.ComplaintRepository;
import com.example.skhubox.repository.NotificationRepository;
import com.example.skhubox.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ComplaintServiceTest {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAllInBatch();
        complaintRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("민원 작성 성공")
    void createComplaint_success() {
        // given
        User user = userRepository.save(
                new User("202100011", "홍길동", "hong1@office.skhu.ac.kr", "IT융합자율학부", "1234")
        );
        ComplaintRequest request = new ComplaintRequest("A-101", "사물함 문이 안 열려요.");

        // when
        ComplaintResponse response = complaintService.createComplaint(user.getStudentNumber(), request);

        // then
        assertNotNull(response.getId());
        assertEquals("A-101", response.getLockerNumber());
        assertEquals("사물함 문이 안 열려요.", response.getContent());
        assertEquals("대기중", response.getStatus());
        assertEquals(user.getStudentNumber(), response.getStudentNumber());

        Complaint savedComplaint = complaintRepository.findById(response.getId()).orElseThrow();
        assertEquals(user.getId(), savedComplaint.getUser().getId());
    }

    @Test
    @DisplayName("내 민원 목록 조회 성공")
    void getMyComplaints_success() {
        // given
        User user = userRepository.save(
                new User("202100011", "홍길동", "hong1@office.skhu.ac.kr", "IT융합자율학부", "1234")
        );
        complaintService.createComplaint(user.getStudentNumber(), new ComplaintRequest("A-101", "첫 번째 민원"));
        complaintService.createComplaint(user.getStudentNumber(), new ComplaintRequest("A-102", "두 번째 민원"));

        // when
        List<ComplaintResponse> responses = complaintService.getMyComplaints(user.getStudentNumber());

        // then
        assertEquals(2, responses.size());
    }

    @Test
    @DisplayName("전체 민원 목록 조회 성공 (관리자용)")
    void getAllComplaints_success() {
        // given
        User user1 = userRepository.save(
                new User("202100011", "홍길동", "hong1@office.skhu.ac.kr", "IT융합자율학부", "1234")
        );
        User user2 = userRepository.save(
                new User("202100022", "김철수", "kim1@office.skhu.ac.kr", "사회융합자율학부", "1234")
        );
        complaintService.createComplaint(user1.getStudentNumber(), new ComplaintRequest("A-101", "유저1의 민원"));
        complaintService.createComplaint(user2.getStudentNumber(), new ComplaintRequest("A-102", "유저2의 민원"));

        // when
        List<ComplaintResponse> responses = complaintService.getAllComplaints();

        // then
        assertEquals(2, responses.size());
    }

    @Test
    @DisplayName("민원 답변 및 상태 변경 성공 (관리자용)")
    void answerComplaint_success() {
        // given
        User user = userRepository.save(
                new User("202100011", "홍길동", "hong1@office.skhu.ac.kr", "IT융합자율학부", "1234")
        );
        ComplaintResponse created = complaintService.createComplaint(user.getStudentNumber(), new ComplaintRequest("A-101", "민원 내용"));
        ComplaintAnswerRequest answerRequest = new ComplaintAnswerRequest(ComplaintStatus.COMPLETED, "처리 완료되었습니다.");

        // when
        ComplaintResponse response = complaintService.answerComplaint(created.getId(), answerRequest);

        // then
        assertEquals("완료", response.getStatus());
        assertEquals("처리 완료되었습니다.", response.getAnswer());

        Complaint savedComplaint = complaintRepository.findById(response.getId()).orElseThrow();
        assertEquals(ComplaintStatus.COMPLETED, savedComplaint.getStatus());
        assertEquals("처리 완료되었습니다.", savedComplaint.getAnswer());
    }
}
