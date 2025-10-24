package com.deodev.transactionService.scheduler;

import com.deodev.transactionService.enums.EventType;
import com.deodev.transactionService.rabbitmq.events.AccountFundedEvent;
import com.deodev.transactionService.rabbitmq.outbox.OutboxEvent;
import com.deodev.transactionService.rabbitmq.outbox.service.OutboxService;
import com.deodev.transactionService.rabbitmq.publisher.TransactionEventsPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventScheduler {

    private final OutboxService outboxService;
    private final TransactionEventsPublisher transactionEventsPublisher;
    private final ObjectMapper mapper;

    @Scheduled(fixedRate = 60000)
    public void scheduleAccountFunding() {
        List<OutboxEvent> outboxEventList = outboxService.findBySentFalseAndEventType(EventType.ACCOUNT_FUNDED);

        log.info("Found {} unsent AccountFunded events", outboxEventList.size());

        int successCount = 0;
        int failCount = 0;

        for (OutboxEvent event : outboxEventList) {
            try {
                AccountFundedEvent accountFundedEvent = mapper.readValue(event.getPayload(), AccountFundedEvent.class);
                transactionEventsPublisher.publishAccountFunded(accountFundedEvent);
                outboxService.setSentEvent(event);
                successCount++;
            } catch (JsonProcessingException ex) {
                log.error("Failed to deserialize payload for OutboxEvent id={} payload={}", event.getId(), event.getPayload(), ex);
                failCount++;
            } catch (Exception ex) {
                log.error("Failed to publish AccountFundedEvent for OutboxEvent id={}", event.getId(), ex);
                failCount++;
            }
        }

        log.info("Finished processing outbox events: success={}, failed={}", successCount, failCount);
    }

}
