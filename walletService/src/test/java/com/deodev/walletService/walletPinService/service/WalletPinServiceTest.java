package com.deodev.walletService.walletPinService.service;

import com.deodev.walletService.exception.PinMismatchException;
import com.deodev.walletService.walletPinService.model.WalletPin;
import com.deodev.walletService.walletPinService.repository.WalletPinRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WalletPinServiceTest {

    @Mock
    private WalletPinRepository walletPinRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private WalletPinService walletPinService;

    private UUID userId;
    private WalletPin walletPin;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        walletPin = WalletPin.builder()
                .userId(userId)
                .pin("encodedPin")
                .build();
    }

    @Test
    void verifyPin_ShouldReturnWalletPin_WhenPinMatches() {
        // given
        when(walletPinRepository.findByUserId(userId)).thenReturn(Optional.of(walletPin));
        when(passwordEncoder.matches("1234", "encodedPin")).thenReturn(true);

        // when
        WalletPin result = walletPinService.verifyPin(userId, "1234", "");

        // then
        assertEquals(walletPin, result);
    }

    @Test
    void verifyPin_ShouldThrowPinMismatchException_WhenPinDoesNotMatch() {
        // given
        when(walletPinRepository.findByUserId(userId)).thenReturn(Optional.of(walletPin));
        when(passwordEncoder.matches("wrongPin", "encodedPin")).thenReturn(false);

        // when & then
        assertThrows(PinMismatchException.class,
                () -> walletPinService.verifyPin(userId, "wrongPin", "Incorrect PIN!"));
    }

    @Test
    void pinsMatch_ShouldThrowPinMismatchException_WhenPinsAreDifferent() {
        // when & then
        assertThrows(PinMismatchException.class,
                () -> walletPinService.pinsMatch("1234", "4321"));
    }
}