package com.deodev.transactionService.scheduler;

import com.deodev.transactionService.enums.EventType;
import com.deodev.transactionService.rabbitmq.events.AccountFundedEvent;
import com.deodev.transactionService.rabbitmq.outbox.OutboxEvent;
import com.deodev.transactionService.rabbitmq.outbox.service.OutboxService;
import com.deodev.transactionService.rabbitmq.publisher.TransactionEventsPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventSchedulerTest {
    @Mock
    private OutboxService outboxService;

    @Mock
    private TransactionEventsPublisher transactionEventsPublisher;

    private final ObjectMapper mapper = new ObjectMapper();

    private OutboxEventScheduler outboxEventScheduler;

    @BeforeEach
    void setup() {
        outboxEventScheduler = new OutboxEventScheduler(
                outboxService,
                transactionEventsPublisher,
                mapper
        );
    }

    @Test
    void scheduleAccountFunding_ShouldPublishEvent_WhenOutboxEventIsNotSentAndIsAccountFundingEvent() throws Exception {
        // given
        AccountFundedEvent accountFundedEvent = AccountFundedEvent.builder()
                .accountNumber("0123456789")
                .amount(100L)
                .build();

        String payload = mapper.writeValueAsString(accountFundedEvent);

        OutboxEvent unsentOutbox = OutboxEvent.builder()
                .eventType(EventType.ACCOUNT_FUNDED)
                .key("account-funded")
                .payload(payload)
                .sent(false)
                .build();

        OutboxEvent unsentOutbox1 = OutboxEvent.builder()
                .eventType(EventType.ACCOUNT_FUNDED)
                .key("account-funded")
                .payload(payload)
                .sent(false)
                .build();

        when(outboxService.findBySentFalseAndEventType(EventType.ACCOUNT_FUNDED))
                .thenReturn(List.of(unsentOutbox, unsentOutbox1));

        ArgumentCaptor<AccountFundedEvent> captor = ArgumentCaptor.forClass(AccountFundedEvent.class);

        // when
        outboxEventScheduler.scheduleAccountFunding();

        // then
        verify(transactionEventsPublisher, times(2)).publishAccountFunded(captor.capture());

        List<AccountFundedEvent> capturedEvents = captor.getAllValues();
        assertThat(capturedEvents).hasSize(2);

        assertThat(capturedEvents.get(0).accountNumber()).isEqualTo("0123456789");
        assertThat(capturedEvents.get(1).accountNumber()).isEqualTo("0123456789");

        verify(outboxService, times(2)).setSentEvent(any());
    }
}