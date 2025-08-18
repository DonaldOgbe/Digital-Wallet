package com.deodev.walletService.walletPinService.service;

import com.deodev.walletService.walletPinService.dto.SetPinRequest;
import com.deodev.walletService.walletPinService.dto.response.CreateWalletPinResponse;
import com.deodev.walletService.walletPinService.model.WalletPin;
import com.deodev.walletService.walletPinService.repository.WalletPinRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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


}