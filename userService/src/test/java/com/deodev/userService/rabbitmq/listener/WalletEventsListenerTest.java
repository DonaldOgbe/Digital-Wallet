package com.deodev.userService.rabbitmq.listener;

import com.deodev.userService.rabbitmq.event.WalletCreatedEvent;
import com.deodev.userService.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.deodev.userService.rabbitmq.constants.keys.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class WalletEventsListenerTest {

    @InjectMocks
    private WalletEventsListener walletEventsListener;

    @Mock
    private UserService userService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void handleWalletLifecycleEvents_ShouldMarkUserVerified_WhenWalletCreated() {
        // given
        WalletCreatedEvent event = new WalletCreatedEvent(userId);

        // when
        walletEventsListener.handleWalletLifecycleEvents(event, WALLET_CREATED);

        // then
        verify(userService).markUserVerified(userId);
    }

    @Test
    void handleWalletLifecycleEvents_ShouldNotCallUserService_WhenRoutingKeyUnknown() {
        // given
        WalletCreatedEvent event = new WalletCreatedEvent(userId);

        // when
        walletEventsListener.handleWalletLifecycleEvents(event, "UNKNOWN_KEY");

        // then
        verifyNoInteractions(userService);
    }

}