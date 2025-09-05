package com.deodev.walletService.walletPinService.service;

import com.deodev.walletService.exception.PinMismatchException;
import com.deodev.walletService.exception.ResourceNotFoundException;
import com.deodev.walletService.walletPinService.dto.request.SetPinRequest;
import com.deodev.walletService.walletPinService.dto.request.UpdatePinRequest;
import com.deodev.walletService.walletPinService.dto.response.CreateWalletPinResponse;
import com.deodev.walletService.walletPinService.dto.response.ValidateWalletPinResponse;
import com.deodev.walletService.walletPinService.model.WalletPin;
import com.deodev.walletService.walletPinService.repository.WalletPinRepository;
import com.deodev.walletService.walletService.model.Wallet;
import com.deodev.walletService.walletService.repository.WalletRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
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

    @Mock
    private WalletRepository walletRepository;

    @Nested
    class createWalletPin {
        @Test
        void createWalletPin_ReturnsSuccess() {
            // given
            UUID walletId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID walletPinId = UUID.randomUUID();

            Wallet wallet = Wallet.builder()
                    .id(walletId)
                    .build();

            SetPinRequest request = SetPinRequest.builder()
                    .newPin("5555")
                    .confirmNewPin("5555")
                    .build();


            // when
            when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
            when(passwordEncoder.encode(request.newPin())).thenReturn("hashedPin");
            when(walletPinRepository.save(any(WalletPin.class))).thenAnswer(
                    invocationOnMock -> {
                        WalletPin walletPin = invocationOnMock.getArgument(0);
                        walletPin.setId(walletPinId);

                        return walletPin;
                    }
            );

            CreateWalletPinResponse response = walletPinService.createPin(request, String.valueOf(userId));

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.timestamp()).isBeforeOrEqualTo(LocalDateTime.now());
            assertThat(response.walletPinId()).isEqualTo(walletPinId);
            assertThat(response.walletId()).isEqualTo(walletId);
            assertThat(response.userId()).isEqualTo(userId);
            verify(passwordEncoder).encode(request.newPin());
            verify(walletRepository).findByUserId(any());
            verify(walletPinRepository).save(any(WalletPin.class));
        }

        @Test
        void createWalletPin_ThrowsResourceNotFound() {
            // given
            UUID userId = UUID.randomUUID();

            SetPinRequest request = SetPinRequest.builder()
                    .newPin("5555")
                    .confirmNewPin("5555")
                    .build();
            when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

            // when + then
            assertThrows(ResourceNotFoundException.class, () -> {
                walletPinService.createPin(request, String.valueOf(userId));
            });

            verify(walletRepository).findByUserId(userId);
        }
    }

    @Nested
    class updatePin {
        @Test
        void updatePin_ReturnsResponse() {
            // given
            UUID walletId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            String hashedPin = "hashedPin";

            UpdatePinRequest request = UpdatePinRequest.builder()
                    .oldPin("5555")
                    .newPin("5555")
                    .confirmNewPin("5555")
                    .build();

            WalletPin walletPin = WalletPin.builder()
                    .id(UUID.randomUUID())
                    .walletId(walletId)
                    .userId(userId)
                    .pin(hashedPin)
                    .pinUpdatedAt(LocalDateTime.now())
                    .build();

            // when
            when(walletPinRepository.findByUserId(userId)).thenReturn(Optional.of(walletPin));
            when(passwordEncoder.matches(request.oldPin(), hashedPin)).thenReturn(true);

            CreateWalletPinResponse response = walletPinService.updatePin(request, String.valueOf(userId));

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.timestamp()).isBeforeOrEqualTo(LocalDateTime.now());
            assertThat(response.walletId()).isEqualTo(walletId);
            assertThat(response.walletPinId()).isEqualTo(walletPin.getId());
            assertThat(walletPin.getUserId()).isEqualTo(response.userId());
            verify(walletPinRepository).findByUserId(userId);
            verify(passwordEncoder).matches(request.oldPin(), hashedPin);
        }

        @Test
        void updatePin_ThrowsResourceNotFound() {
            // given
            UpdatePinRequest request = UpdatePinRequest.builder()
                    .oldPin("1111")
                    .newPin("1234")
                    .confirmNewPin("1234")
                    .build();

            UUID userId = UUID.randomUUID();

            when(walletPinRepository.findByUserId(userId)).thenReturn(Optional.empty());

            // when & then
            assertThrows(ResourceNotFoundException.class,
                    () -> walletPinService.updatePin(request, String.valueOf(userId)));

            verify(walletPinRepository).findByUserId(userId);
        }

        @Test
        void updatePin_ThrowsPinMisMatch() {
            // given
            UUID userId = UUID.randomUUID();
            WalletPin walletPin = WalletPin.builder()
                    .id(UUID.randomUUID())
                    .pin("hashed-pin")
                    .build();

            UpdatePinRequest request = UpdatePinRequest.builder()
                    .oldPin("1111")
                    .newPin("1234")
                    .confirmNewPin("1234")
                    .build();

            when(walletPinRepository.findByUserId(userId)).thenReturn(Optional.of(walletPin));
            when(passwordEncoder.matches(request.oldPin(), walletPin.getPin())).thenReturn(false);

            // when & then
            assertThrows(PinMismatchException.class,
                    () -> walletPinService.updatePin(request, String.valueOf(userId)));

            verify(walletPinRepository).findByUserId(userId);
            verify(passwordEncoder).matches(request.oldPin(), walletPin.getPin());
        }
    }

    @Nested
    class validatePin {
        @Test
        void validatePin_ReturnsSuccess() {
            // given
            UUID userId = UUID.randomUUID();
            String rawPin = "1234";
            String storedPin = "hashed-pin";

            WalletPin walletPin = WalletPin.builder()
                    .userId(userId)
                    .pin(storedPin)
                    .build();

            when(walletPinRepository.findByUserId(userId))
                    .thenReturn(Optional.of(walletPin));

            when(passwordEncoder.matches(rawPin, storedPin)).thenReturn(true);

            // when
            ValidateWalletPinResponse response = walletPinService.validatePin(String.valueOf(userId), rawPin);

            // then
            assertThat(response.isValid()).isTrue();
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.timestamp()).isBeforeOrEqualTo(LocalDateTime.now());

            verify(walletPinRepository).findByUserId(userId);
            verify(passwordEncoder).matches(rawPin, storedPin);

        }

        @Test
        void validatePin_ThrowsResourceNotFound() {
            // given
            UUID userId = UUID.randomUUID();
            String rawPin = "1234";

            when(walletPinRepository.findByUserId(userId))
                    .thenReturn(Optional.empty());

            // when + then
            assertThrows(ResourceNotFoundException.class, () -> {
                walletPinService.validatePin(String.valueOf(userId), rawPin);
            });

            verify(walletPinRepository).findByUserId(userId);
        }

        @Test
        void validatePin_ThrowsPinMisMatch() {
            // given
            UUID userId = UUID.randomUUID();
            String rawPin = "1234";
            String storedPin = "hashed-pin";

            WalletPin walletPin = WalletPin.builder()
                    .userId(userId)
                    .pin(storedPin)
                    .build();

            when(walletPinRepository.findByUserId(userId))
                    .thenReturn(Optional.of(walletPin));

            when(passwordEncoder.matches(rawPin, storedPin)).thenReturn(false);

            // when + then
            assertThrows(PinMismatchException.class, () -> {
                walletPinService.validatePin(String.valueOf(userId), rawPin);
            });

            verify(walletPinRepository).findByUserId(userId);
            verify(passwordEncoder).matches(rawPin, storedPin);

        }
    }

    @Test
    void pinsMatch_ThrowsPinMismatch() {
        // when & then
        assertThrows(
                PinMismatchException.class,
                () -> walletPinService.pinsMatch("1234", "1324")
        );
    }

}