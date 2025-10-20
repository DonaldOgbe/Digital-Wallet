package com.deodev.transactionService.transactionService.model;

import com.deodev.transactionService.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "card_funding_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardFundingTransaction {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "txn_id", nullable = false, updatable = false)
    private UUID transactionId;

    @Column(name = "acct_no", nullable = false, length = 10)
    private String accountNumber;

    @Column(name = "card_last4", length = 4)
    private String cardLast4;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type")
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    @Column(name = "gateway", nullable = false)
    private PaymentGateway paymentGateway;

    @Column(name = "gateway_ref")
    private String gatewayReference;

    @Column(name = "gateway_txn_id")
    private Long gatewayTransactionId;

    @Column(name = "auth_code")
    private String authorizationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;
}
