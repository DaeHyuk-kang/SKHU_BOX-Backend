package com.example.skhubox.domain.queue;

import com.example.skhubox.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "queue_mode_settings")
public class QueueModeSetting extends BaseEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private boolean enabled;

    public QueueModeSetting(Long id, boolean enabled) {
        this.id = id;
        this.enabled = enabled;
    }

    public void changeEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}