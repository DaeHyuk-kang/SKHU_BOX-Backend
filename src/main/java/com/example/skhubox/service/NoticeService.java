package com.example.skhubox.service;

import com.example.skhubox.domain.notice.Notice;
import com.example.skhubox.domain.operation.OperationLogType;
import com.example.skhubox.dto.notice.NoticeCreateRequest;
import com.example.skhubox.dto.notice.NoticeResponse;
import com.example.skhubox.dto.notice.NoticeUpdateRequest;
import com.example.skhubox.exception.BusinessException;
import com.example.skhubox.exception.ErrorCode;
import com.example.skhubox.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final OperationLogService operationLogService;

    public List<NoticeResponse> getLatestNotices() {
        return noticeRepository.findTop5ByDeletedFalseOrderByPinnedDescCreatedAtDesc()
                .stream()
                .map(NoticeResponse::from)
                .toList();
    }

    public List<NoticeResponse> getAllNotices() {
        return noticeRepository.findAllByDeletedFalseOrderByPinnedDescCreatedAtDesc()
                .stream()
                .map(NoticeResponse::from)
                .toList();
    }

    @Transactional
    public NoticeResponse createNotice(NoticeCreateRequest request) {
        Notice notice = noticeRepository.save(new Notice(
                request.getTitle(),
                request.getContent(),
                request.isPinned()
        ));
        operationLogService.log(OperationLogType.NOTICE_POSTED, "공지사항 등록", request.getTitle());
        return NoticeResponse.from(notice);
    }

    @Transactional
    public NoticeResponse updateNotice(Long id, NoticeUpdateRequest request) {
        Notice notice = noticeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
        
        notice.update(request.getTitle(), request.getContent(), request.isPinned());
        noticeRepository.flush(); // 로그 찍기 전 DB 반영 보장
        
        operationLogService.log(OperationLogType.NOTICE_UPDATED, "공지사항 수정", request.getTitle());
        return NoticeResponse.from(notice);
    }

    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
        
        notice.delete();
        noticeRepository.flush(); // 로그 찍기 전 DB 반영 보장
        
        operationLogService.log(OperationLogType.NOTICE_DELETED, "공지사항 삭제", notice.getTitle());
    }
}
