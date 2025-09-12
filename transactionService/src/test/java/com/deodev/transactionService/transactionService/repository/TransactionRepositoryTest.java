package com.deodev.transactionService.transactionService.repository;

import com.deodev.transactionService.enums.TransactionStatus;
import com.deodev.transactionService.transactionService.model.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void shouldCheckTransactionExistsById() {
        // given
        Transaction transaction = Transaction.builder()
                .senderAccountNumber("1234567890")
                .receiverAccountNumber("0987654321")
                .amount(1000L)
                .status(TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);

        // when
        boolean exists = transactionRepository.existsById(saved.getId());

        // then
        assertThat(exists).isTrue();
    }
}