package com.deodev.transactionService.rabbitmq.outbox.repository;

import com.deodev.transactionService.rabbitmq.outbox.OutboxEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OutboxRepositoryTest {

    @Autowired
    private OutboxRepository outboxRepository;

    @Test
    void findBySentFalse_ShouldReturnOnlyUnsentOutboxEntries() {
        // given
        OutboxEvent sentOutbox = OutboxEvent.builder()
                .eventType("account-funded")
                .payload("payload")
                .sent(true).build();

        OutboxEvent unsentOutbox = OutboxEvent.builder()
                .eventType("account-funded")
                .payload("payload")
                .sent(false).build();

        OutboxEvent unsentOutbox1 = OutboxEvent.builder()
                .eventType("account-funded")
                .payload("payload")
                .sent(false).build();


        unsentOutbox.setSent(false);

        outboxRepository.saveAll(List.of(sentOutbox, unsentOutbox, unsentOutbox1));

        // when
        List<OutboxEvent> unsentList = outboxRepository.findBySentFalse();

        // then
        assertThat(unsentList).hasSize(2);
        assertThat(unsentList.getFirst().isSent()).isFalse();
    }
}
