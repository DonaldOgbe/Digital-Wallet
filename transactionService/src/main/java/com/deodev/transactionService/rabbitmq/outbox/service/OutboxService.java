package com.deodev.transactionService.rabbitmq.outbox.service;

import com.deodev.transactionService.rabbitmq.outbox.OutboxEvent;
import com.deodev.transactionService.rabbitmq.outbox.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private ObjectMapper mapper;
    private OutboxRepository outboxRepository;

    public <T> void createScheduledEvent(String eventType, T event) throws Exception{
        String payload = mapper.writeValueAsString(event);

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventType(eventType).payload(payload).sent(false).build();

        save(outboxEvent);
    }

    public void setSentEvent(OutboxEvent outboxEvent) {
        outboxEvent.setSent(true);
        save(outboxEvent);
    }

    void save(OutboxEvent outboxEvent) {
        outboxRepository.save(outboxEvent);
    }
}
