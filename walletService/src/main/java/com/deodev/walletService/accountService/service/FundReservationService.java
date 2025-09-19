package com.deodev.walletService.accountService.service;

import com.deodev.walletService.accountService.model.FundReservation;
import com.deodev.walletService.accountService.repository.FundReservationRepository;
import com.deodev.walletService.enums.FundReservationStatus;
import com.deodev.walletService.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FundReservationService {

    private final FundReservationRepository fundReservationRepository;

    public FundReservation createNewReservation(String accountNumber, UUID transactionId, Long amount) {
        FundReservation fundReservation = FundReservation.builder()
                .accountNumber(accountNumber)
                .transactionId(transactionId)
                .amount(amount)
                .build();

        return fundReservationRepository.save(fundReservation);
    }

    public void setUsedReservation(FundReservation reservation) {
        reservation.setStatus(FundReservationStatus.USED);
        reservation.setUsedAt(LocalDateTime.now());
        fundReservationRepository.save(reservation);
    }

    public void setReleasedReservation(FundReservation reservation) {
        reservation.setStatus(FundReservationStatus.RELEASED);
        reservation.setReleasedAt(LocalDateTime.now());
        fundReservationRepository.save(reservation);
    }

    public FundReservation findByTransactionId(UUID transactionId) {
        return fundReservationRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Fund reservation not found"));
    }
}
