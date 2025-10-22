package com.deodev.transactionService.rabbitmq.outbox.repository;

import com.deodev.transactionService.enums.EventType;
import com.deodev.transactionService.rabbitmq.outbox.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findBySentFalseAndEventType(EventType eventType);
}
