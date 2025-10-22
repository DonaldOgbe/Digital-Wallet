package com.deodev.transactionService.rabbitmq.outbox.service;

import com.deodev.transactionService.enums.EventType;
import com.deodev.transactionService.rabbitmq.outbox.OutboxEvent;
import com.deodev.transactionService.rabbitmq.outbox.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final ObjectMapper mapper;
    private final OutboxRepository outboxRepository;

    public <T> void createScheduledEvent(String id, String key, EventType eventType, T event) throws Exception{
        String payload = mapper.writeValueAsString(event);

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(UUID.fromString(id)).key(key).eventType(eventType).payload(payload).sent(false).build();

        save(outboxEvent);
    }

    public void setSentEvent(OutboxEvent outboxEvent) {
        outboxEvent.setSent(true);
        save(outboxEvent);
    }

    public List<OutboxEvent> findBySentFalseAndEventType(EventType eventType) {
        return outboxRepository.findBySentFalseAndEventType(eventType);
    }

    void save(OutboxEvent outboxEvent) {
        outboxRepository.save(outboxEvent);
    }
}
