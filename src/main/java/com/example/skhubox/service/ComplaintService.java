package com.example.skhubox.service;

import com.example.skhubox.dto.complaint.ComplaintAnswerRequest;
import com.example.skhubox.dto.complaint.ComplaintRequest;
import com.example.skhubox.dto.complaint.ComplaintResponse;

import java.util.List;

public interface ComplaintService {
    ComplaintResponse createComplaint(String studentNumber, ComplaintRequest request);
    ComplaintResponse getComplaintDetail(String studentNumber, Long complaintId);
    List<ComplaintResponse> getMyComplaints(String studentNumber);
    List<ComplaintResponse> getAllComplaints();
    ComplaintResponse answerComplaint(Long complaintId, ComplaintAnswerRequest request);
    void cancelComplaint(String studentNumber, Long complaintId);
}
