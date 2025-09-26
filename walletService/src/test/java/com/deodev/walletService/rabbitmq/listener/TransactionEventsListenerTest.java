package com.deodev.walletService.rabbitmq.listener;

import com.deodev.walletService.accountService.service.AccountService;
import com.deodev.walletService.enums.ErrorCode;
import com.deodev.walletService.exception.FundReservationException;
import com.deodev.walletService.exception.ResourceNotFoundException;
import com.deodev.walletService.rabbitmq.events.P2PTransferRequestedEvent;
import com.deodev.walletService.rabbitmq.publisher.WalletEventsPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static com.deodev.walletService.rabbitmq.constants.keys.*;

@ExtendWith(MockitoExtension.class)
class TransactionEventsListenerTest {

    @InjectMocks
    @Spy
    private TransactionEventsListener listener;

    @Mock
    private AccountService accountService;

    @Mock
    private WalletEventsPublisher walletEventsPublisher;

    private P2PTransferRequestedEvent event;

    @BeforeEach
    void setUp() {
        event = new P2PTransferRequestedEvent(
                "0123456789",
                UUID.randomUUID()
        );
    }

    @Test
    void handleP2PTransferRequested_ShouldHandleResourceNotFoundException() {
        // given
        ResourceNotFoundException ex = new ResourceNotFoundException("User not found");
        doThrow(ex).when(accountService).transferFunds(event);

        // when
        listener.handleP2PTransferRequested(event, P2P_TRANSFER_REQUESTED);

        // then
        verify(listener).handleException(
                eq("User not found"),
                eq(ErrorCode.NOT_FOUND),
                eq(event.transactionId()),
                eq(ex)
        );
    }

    @Test
    void handleP2PTransferRequested_ShouldHandleFundReservationException() {
        // given
        FundReservationException ex = new FundReservationException("Fund reservation expired");
        doThrow(ex).when(accountService).transferFunds(event);

        // when
        listener.handleP2PTransferRequested(event, P2P_TRANSFER_REQUESTED);

        // then
        verify(listener).handleException(
                eq("Fund reservation expired"),
                eq(ErrorCode.FUND_RESERVATION_ERROR),
                eq(event.transactionId()),
                eq(ex)
        );
    }

    @Test
    void handleP2PTransferRequested_ShouldHandleUnexpectedException() {
        // given
        Exception ex = new RuntimeException("Something went wrong");
        doThrow(ex).when(accountService).transferFunds(event);

        // when
        listener.handleP2PTransferRequested(event, P2P_TRANSFER_REQUESTED);

        // then
        verify(listener).handleException(
                eq("Something went wrong"),
                eq(ErrorCode.SYSTEM_ERROR),
                eq(event.transactionId()),
                any()
        );
    }

}