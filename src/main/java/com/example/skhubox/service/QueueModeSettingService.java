package com.example.skhubox.service;

import com.example.skhubox.domain.queue.QueueModeSetting;
import com.example.skhubox.dto.admin.QueueModeResponse;
import com.example.skhubox.repository.QueueModeSettingRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class QueueModeSettingService {

    private static final Long QUEUE_MODE_SETTING_ID = 1L;

    private final QueueModeSettingRepository queueModeSettingRepository;
    private final WaitingQueueService waitingQueueService;

    @PostConstruct
    public void init() {
        if (!queueModeSettingRepository.existsById(QUEUE_MODE_SETTING_ID)) {
            queueModeSettingRepository.save(new QueueModeSetting(QUEUE_MODE_SETTING_ID, false));
        }
    }

    @Transactional(readOnly = true)
    public QueueModeResponse getQueueMode() {
        return new QueueModeResponse(isQueueModeEnabled());
    }

    public QueueModeResponse updateQueueMode(boolean enabled) {
        QueueModeSetting setting = queueModeSettingRepository.findById(QUEUE_MODE_SETTING_ID)
                .orElseGet(() -> new QueueModeSetting(QUEUE_MODE_SETTING_ID, false));

        setting.changeEnabled(enabled);
        queueModeSettingRepository.save(setting);

        if (!enabled) {
            waitingQueueService.clearAllQueues();
        }

        return new QueueModeResponse(setting.isEnabled());
    }

    @Transactional(readOnly = true)
    public boolean isQueueModeEnabled() {
        return queueModeSettingRepository.findById(QUEUE_MODE_SETTING_ID)
                .map(QueueModeSetting::isEnabled)
                .orElse(false);
    }
}