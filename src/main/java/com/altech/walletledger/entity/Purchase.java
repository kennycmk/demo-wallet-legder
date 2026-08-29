package com.altech.walletledger.entity;

import com.altech.walletledger.enums.PurchaseStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "purchases")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Purchase {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, updatable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PurchaseStatus status;

    @Column(name = "idempotency_key", nullable = false, updatable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    public static Purchase pending(UUID userId, BigDecimal amount, String idempotencyKey) {
        Purchase purchase = new Purchase();
        purchase.id = UUID.randomUUID();
        purchase.userId = userId;
        purchase.amount = amount;
        purchase.status = PurchaseStatus.PENDING;
        purchase.idempotencyKey = idempotencyKey;
        return purchase;
    }

    public void markPaid() {
        status = PurchaseStatus.PAID;
        paidAt = Instant.now();
    }

    public void markFailed() {
        status = PurchaseStatus.FAILED;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
