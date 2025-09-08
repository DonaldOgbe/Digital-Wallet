package com.deodev.walletService.accountService.repository;

import com.deodev.walletService.accountService.model.FundReservation;
import com.deodev.walletService.enums.FundReservationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class FundReservationRepositoryTest {
    @Autowired
    private FundReservationRepository fundReservationRepository;

    @Test
    void testItWorks() {
        // given

        String accountNumber = "0123456789";


        FundReservation reservation = FundReservation.builder()
                .accountNumber(accountNumber)
                .transactionId(UUID.randomUUID())
                .amount(1000L)
                .build();

        fundReservationRepository.save(reservation);

        // when
        boolean result = fundReservationRepository.existsByAccountNumber(accountNumber);

        // then
        assertThat(result).isTrue();
    }

}