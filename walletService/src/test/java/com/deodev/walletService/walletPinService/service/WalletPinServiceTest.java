package com.deodev.walletService.walletPinService.service;

import com.deodev.walletService.exception.PinMismatchException;
import com.deodev.walletService.walletPinService.dto.request.SetPinRequest;
import com.deodev.walletService.walletPinService.dto.request.UpdatePinRequest;
import com.deodev.walletService.walletPinService.dto.response.CreateWalletPinResponse;
import com.deodev.walletService.walletPinService.model.WalletPin;
import com.deodev.walletService.walletPinService.repository.WalletPinRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class WalletPinServiceTest {

    @InjectMocks
    private WalletPinService walletPinService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private WalletPinRepository walletPinRepository;

    @Test
    void testThatWalletPinIsCreated() {
        // given
        UUID walletId = UUID.randomUUID();

        SetPinRequest request = SetPinRequest.builder()
                .newPin("5555")
                .confirmNewPin("5555")
                .build();

        WalletPin walletPin = WalletPin.builder()
                .pin("hashedPin")
                .walletId(walletId)
                .build();

        // when
        when(passwordEncoder.encode(request.newPin())).thenReturn(walletPin.getPin());
        when(walletPinRepository.save(any(WalletPin.class))).thenReturn(walletPin);

        CreateWalletPinResponse response = walletPinService.createPin(request, String.valueOf(walletId));

        // then
        assertThat(walletPin.getId()).isEqualTo(response.walletPinId());
        assertThat(walletPin.getWalletId()).isEqualTo(response.walletId());
        verify(passwordEncoder).encode(request.newPin());
        verify(walletPinRepository).save(any(WalletPin.class));
    }

    @Test
    void testCreatePinThrowsPinMismatchException() {
        // when & then
        assertThrows(
                PinMismatchException.class,
                () -> walletPinService.pinsMatch("1234", "1324")
        );
    }

    @Test
    void testThatPinIsUpdated() {
        // given
        UUID walletId = UUID.randomUUID();

        UpdatePinRequest request = UpdatePinRequest.builder()
                .oldPin("5555")
                .newPin("5555")
                .confirmNewPin("5555")
                .build();

        WalletPin walletPin = WalletPin.builder()
                .id(UUID.randomUUID())
                .walletId(walletId)
                .pin("5555")
                .pinUpdatedAt(LocalDateTime.now())
                .build();

        // when
        when(walletPinRepository.findByWalletId(any(UUID.class))).thenReturn(Optional.of(walletPin));
        when(passwordEncoder.matches(request.oldPin(), walletPin.getPin())).thenReturn(true);

        CreateWalletPinResponse response = walletPinService.updatePin(request, String.valueOf(walletId));

        // then
        assertThat(response.walletId()).isEqualTo(walletId);
        assertThat(response.walletPinId()).isEqualTo(walletPin.getId());
    }

    @Test
    void testThatIllegalArgumentErrorIsThrownWhenWalletNotFound() {
        // given
        UpdatePinRequest request = UpdatePinRequest.builder()
                .oldPin("1111")
                .newPin("1234")
                .confirmNewPin("1234")
                .build();

        String walletId = String.valueOf(UUID.randomUUID());

        when(walletPinRepository.findByWalletId(any(UUID.class))).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> walletPinService.updatePin(request, walletId));
    }

    @Test
    void testThatIllegalArgumentErrorIsThrownWhenOldPinDoesNotMatch() {
        // given
        UUID walletId = UUID.randomUUID();
        WalletPin walletPin = WalletPin.builder()
                .id(UUID.randomUUID())
                .walletId(walletId)
                .pin("hashed-pin")
                .build();

        UpdatePinRequest request = UpdatePinRequest.builder()
                .oldPin("1111")
                .newPin("1234")
                .confirmNewPin("1234")
                .build();

        when(walletPinRepository.findByWalletId(walletId)).thenReturn(Optional.of(walletPin));
        when(passwordEncoder.matches("1111", "hashed-pin")).thenReturn(false);

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> walletPinService.updatePin(request, String.valueOf(walletId)));
    }


}