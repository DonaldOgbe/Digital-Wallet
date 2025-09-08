package com.deodev.walletService.accountService.model;


import com.deodev.walletService.enums.FundReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class FundReservation {
    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID id;

    @JoinColumn(nullable = false, updatable = false, unique = true)
    private String accountNumber;

    @Column(name = "transaction_id", nullable = false, updatable = false, unique = true)
    private UUID transactionId;

    @Column(nullable = false)
    private long amount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FundReservationStatus status = FundReservationStatus.ACTIVE;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt = LocalDateTime.now().plusHours(1);

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
