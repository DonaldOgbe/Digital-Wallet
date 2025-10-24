package com.deodev.transactionService.rabbitmq.outbox;

import com.deodev.transactionService.enums.EventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "transaction_outbox_events")
public class OutboxEvent {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "event_key", updatable = false, nullable = false)
    private String key;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", updatable = false, nullable = false)
    private EventType eventType;

    @Column(updatable = false, nullable = false)
    private String payload;

    @Column(nullable = false)
    private boolean sent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
