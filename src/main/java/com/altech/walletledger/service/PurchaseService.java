package com.altech.walletledger.service;

import com.altech.walletledger.entity.Purchase;
import com.altech.walletledger.enums.PaymentEvent;
import com.altech.walletledger.enums.PurchaseStatus;
import com.altech.walletledger.enums.TransactionReason;
import com.altech.walletledger.exception.PurchaseNotFoundException;
import com.altech.walletledger.exception.PurchaseNotPayableException;
import com.altech.walletledger.repository.PurchaseRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final WalletService walletService;
    private final TransactionTemplate transactionTemplate;

    public PurchaseService(
            PurchaseRepository purchaseRepository,
            WalletService walletService,
            PlatformTransactionManager transactionManager
    ) {
        this.purchaseRepository = purchaseRepository;
        this.walletService = walletService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public Purchase create(UUID userId, BigDecimal amount, String idempotencyKey) {
        try {
            return transactionTemplate.execute(status ->
                    purchaseRepository.findByIdempotencyKey(idempotencyKey)
                            .orElseGet(() -> purchaseRepository.saveAndFlush(
                                    Purchase.pending(userId, amount, idempotencyKey))));
        } catch (DataIntegrityViolationException ex) {
            return purchaseRepository.findByIdempotencyKey(idempotencyKey).orElseThrow(() -> ex);
        }
    }

    @Transactional
    public Purchase handlePayment(UUID purchaseId, PaymentEvent event) {
        Purchase purchase = purchaseRepository.findByIdForUpdate(purchaseId)
                .orElseThrow(() -> new PurchaseNotFoundException(purchaseId));
        if (purchase.getStatus() == PurchaseStatus.PAID) {
            return purchase;
        }
        if (purchase.getStatus() == PurchaseStatus.FAILED) {
            throw new PurchaseNotPayableException();
        }
        if (event == PaymentEvent.PAYMENT_FAILED) {
            purchase.markFailed();
            return purchase;
        }
        walletService.credit(
                purchase.getUserId(),
                purchase.getAmount(),
                TransactionReason.PURCHASE,
                "purchase:" + purchase.getId()
        );
        purchase.markPaid();
        return purchase;
    }
}
