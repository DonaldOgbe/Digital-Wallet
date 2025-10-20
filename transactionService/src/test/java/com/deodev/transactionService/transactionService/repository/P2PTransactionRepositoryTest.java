package com.deodev.transactionService.transactionService.repository;

import com.deodev.transactionService.enums.Currency;
import com.deodev.transactionService.enums.TransactionStatus;
import com.deodev.transactionService.transactionService.model.P2PTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class P2PTransactionRepositoryTest {

    @Autowired
    private P2PTransactionRepository p2PTransactionRepository;

    @Test
    void shouldCheckTransactionExistsById() {
        // given
        P2PTransaction p2PTransaction = P2PTransaction.builder()
                .transactionId(UUID.randomUUID())
                .senderAccountNumber("1234567890")
                .receiverAccountNumber("0987654321")
                .amount(1000L)
                .currency(Currency.NGN)
                .status(TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        P2PTransaction saved = p2PTransactionRepository.save(p2PTransaction);

        // when
        boolean exists = p2PTransactionRepository.existsById(saved.getId());

        // then
        assertThat(exists).isTrue();
    }
}