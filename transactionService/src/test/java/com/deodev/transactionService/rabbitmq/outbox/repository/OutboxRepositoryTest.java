package com.deodev.transactionService.rabbitmq.outbox.repository;

import com.deodev.transactionService.enums.EventType;
import com.deodev.transactionService.rabbitmq.outbox.OutboxEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OutboxRepositoryTest {

    @Autowired
    private OutboxRepository outboxRepository;

    @Test
    void findBySentFalse_ShouldReturnOnlyUnsentOutboxEntries() {
        // given
        OutboxEvent sentOutbox = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .eventType(EventType.ACCOUNT_FUNDED)
                .key("account-funded")
                .payload("payload")
                .sent(true).build();

        OutboxEvent unsentOutbox = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .eventType(EventType.ACCOUNT_FUNDED)
                .key("account-funded")
                .payload("payload")
                .sent(false).build();

        OutboxEvent unsentOutbox1 = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .eventType(EventType.ACCOUNT_FUNDED)
                .key("account-funded")
                .payload("payload")
                .sent(false).build();

        outboxRepository.saveAll(List.of(sentOutbox, unsentOutbox, unsentOutbox1));

        // when
        List<OutboxEvent> unsentList = outboxRepository.findBySentFalseAndEventType(EventType.ACCOUNT_FUNDED);

        // then
        assertThat(unsentList).hasSize(2);
        assertThat(unsentList.getFirst().isSent()).isFalse();
    }
}
