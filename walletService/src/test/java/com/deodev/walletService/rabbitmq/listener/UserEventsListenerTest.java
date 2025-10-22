package com.deodev.walletService.rabbitmq.listener;

import com.deodev.walletService.rabbitmq.events.UserRegisteredEvent;
import com.deodev.walletService.walletService.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static com.deodev.walletService.rabbitmq.constants.keys.*;

@ExtendWith(MockitoExtension.class)
class UserEventsListenerTest {
    @InjectMocks
    private UserEventsListener userEventsListener;

    @Mock
    private WalletService walletService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void handleUserEvents_ShouldCallCreateWallet_WhenUserCreatedEvent() {
        // given
        UserRegisteredEvent event = new UserRegisteredEvent(userId);

        // when
        userEventsListener.handleUserEvents(event, USER_CREATED);

        // then
        verify(walletService).createWallet(userId.toString());
    }

    @Test
    void handleUserEvents_ShouldNotCallWalletService_WhenRoutingKeyUnknown() {
        // given
        UserRegisteredEvent event = new UserRegisteredEvent(userId);

        // when
        userEventsListener.handleUserEvents(event, "UNKNOWN_KEY");

        // then
        verifyNoInteractions(walletService);
    }


}