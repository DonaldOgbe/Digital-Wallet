package com.deodev.transactionService.transactionService.repository;

import com.deodev.transactionService.enums.CardType;
import com.deodev.transactionService.enums.PaymentGateway;
import com.deodev.transactionService.enums.TransactionStatus;
import com.deodev.transactionService.transactionService.model.CardFundingTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class CardFundingTransactionRepositoryTest {

    @Autowired
    private CardFundingTransactionRepository repository;

    @Test
    void shouldCheckTransactionExistsByGatewayReference() {
        // given
        CardFundingTransaction txn = CardFundingTransaction.builder()
                .transactionId(UUID.randomUUID())
                .accountNumber("1234567890")
                .cardLast4("1234")
                .cardType(CardType.MASTERCARD)
                .paymentGateway(PaymentGateway.FLUTTERWAVE)
                .gatewayReference("REF-12345")
                .gatewayTransactionId(1245867L)
                .authorizationCode("AUTH-9999")
                .status(TransactionStatus.PENDING)
                .build();

        repository.save(txn);

        // when
        boolean exists = repository.existsByGatewayReference("REF-12345");

        // then
        assertThat(exists).isTrue();
    }
}