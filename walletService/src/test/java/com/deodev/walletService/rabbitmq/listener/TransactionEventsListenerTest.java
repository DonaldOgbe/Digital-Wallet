package com.deodev.walletService.rabbitmq.listener;

import com.deodev.walletService.accountService.service.AccountService;
import com.deodev.walletService.rabbitmq.events.AccountFundedEvent;
import com.deodev.walletService.redis.RedisCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.deodev.walletService.rabbitmq.constants.keys.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionEventsListenerTest {

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransactionEventsListener transactionEventsListener;

    @Test
    void handleTransactionEvents_ShouldCreditAccount_WhenFirstTimeEvent() {
        // given
        AccountFundedEvent event = new AccountFundedEvent("evt-123", "0123456789", 1000L);
        when(redisCacheService.setIfAbsent(event.eventId())).thenReturn(true);

        // when
        transactionEventsListener.handleTransactionEvents(event, ACCOUNT_FUNDED);

        // then
        verify(redisCacheService).setIfAbsent(event.eventId());
        verify(accountService).creditBalance(event.accountNumber(), event.amount());
    }

    @Test
    void handleTransactionEvents_ShouldSkip_WhenEventAlreadyProcessed() {
        // given
        AccountFundedEvent event = new AccountFundedEvent("evt-456", "0123456789", 500L);
        when(redisCacheService.setIfAbsent(event.eventId())).thenReturn(false);

        // when
        transactionEventsListener.handleTransactionEvents(event, ACCOUNT_FUNDED);

        // then
        verify(redisCacheService).setIfAbsent(event.eventId());
        verifyNoInteractions(accountService);
    }

    @Test
    void handleTransactionEvents_ShouldNotCreditAccount_WhenRoutingKeyUnknown() {
        // given
        AccountFundedEvent event = new AccountFundedEvent("evt-789", "0123456789", 200L);

        // when
        transactionEventsListener.handleTransactionEvents(event, "UNKNOWN_KEY");

        // then
        verifyNoInteractions(redisCacheService);
        verifyNoInteractions(accountService);
    }
}