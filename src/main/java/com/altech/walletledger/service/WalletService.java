package com.altech.walletledger.service;

import com.altech.walletledger.constant.AppConstants;
import com.altech.walletledger.entity.Transaction;
import com.altech.walletledger.entity.User;
import com.altech.walletledger.entity.Wallet;
import com.altech.walletledger.enums.EntryType;
import com.altech.walletledger.enums.TransactionReason;
import com.altech.walletledger.exception.SelfTransferException;
import com.altech.walletledger.exception.UserNotFoundException;
import com.altech.walletledger.exception.WalletNotFoundException;
import com.altech.walletledger.repository.TransactionRepository;
import com.altech.walletledger.repository.UserRepository;
import com.altech.walletledger.repository.WalletRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class WalletService {

    public record TransferResult(Wallet sender, Transaction debit, Transaction credit) {
    }

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final TransactionTemplate transactionTemplate;

    public WalletService(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public Transaction credit(
            UUID userId,
            BigDecimal amount,
            TransactionReason reason,
            String idempotencyKey
    ) {
        return runIdempotent(idempotencyKey, () ->
                apply(userId, amount, EntryType.CREDIT, reason, idempotencyKey));
    }

    public Transaction debit(
            UUID userId,
            BigDecimal amount,
            TransactionReason reason,
            String idempotencyKey
    ) {
        return runIdempotent(idempotencyKey, () ->
                apply(userId, amount, EntryType.DEBIT, reason, idempotencyKey));
    }

    public TransferResult transfer(
            UUID fromUserId,
            String recipientEmail,
            BigDecimal amount,
            String idempotencyKey
    ) {
        String debitKey = idempotencyKey + AppConstants.TRANSFER_OUT_SUFFIX;
        String creditKey = idempotencyKey + AppConstants.TRANSFER_IN_SUFFIX;
        try {
            return transactionTemplate.execute(status ->
                    existingTransfer(debitKey, creditKey)
                            .orElseGet(() -> executeTransfer(fromUserId, recipientEmail, amount, debitKey, creditKey)));
        } catch (DataIntegrityViolationException ex) {
            return existingTransfer(debitKey, creditKey).orElseThrow(() -> ex);
        }
    }

    @Transactional(readOnly = true)
    public Wallet getWallet(UUID userId) {
        return walletRepository.findById(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));
    }

    @Transactional(readOnly = true)
    public Page<Transaction> getHistory(UUID userId, int page, int size) {
        if (!walletRepository.existsById(userId)) {
            throw new WalletNotFoundException(userId);
        }
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }

    private java.util.Optional<TransferResult> existingTransfer(String debitKey, String creditKey) {
        return transactionRepository.findByIdempotencyKey(debitKey).flatMap(debit ->
                transactionRepository.findByIdempotencyKey(creditKey).map(credit ->
                        new TransferResult(getWallet(debit.getUserId()), debit, credit)));
    }

    private TransferResult executeTransfer(
            UUID fromUserId,
            String recipientEmail,
            BigDecimal amount,
            String debitKey,
            String creditKey
    ) {
        User recipient = userRepository.findByEmail(recipientEmail.toLowerCase())
                .orElseThrow(() -> new UserNotFoundException(recipientEmail));
        if (recipient.getId().equals(fromUserId)) {
            throw new SelfTransferException();
        }

        Wallet[] wallets = lockBoth(fromUserId, recipient.getId());
        return existingTransfer(debitKey, creditKey).orElseGet(() -> {
            Transaction debitTxn = persist(wallets[0], amount, EntryType.DEBIT, TransactionReason.TRANSFER, debitKey);
            Transaction creditTxn = persist(wallets[1], amount, EntryType.CREDIT, TransactionReason.TRANSFER, creditKey);
            return new TransferResult(wallets[0], debitTxn, creditTxn);
        });
    }

    private Wallet[] lockBoth(UUID fromUserId, UUID toUserId) {
        UUID firstId = fromUserId.compareTo(toUserId) < 0 ? fromUserId : toUserId;
        UUID secondId = fromUserId.compareTo(toUserId) < 0 ? toUserId : fromUserId;
        Wallet first = walletRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new WalletNotFoundException(firstId));
        Wallet second = walletRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new WalletNotFoundException(secondId));
        Wallet sender = fromUserId.equals(firstId) ? first : second;
        Wallet receiver = fromUserId.equals(firstId) ? second : first;
        return new Wallet[] {sender, receiver};
    }

    private Transaction runIdempotent(String idempotencyKey, Supplier<Transaction> action) {
        try {
            return transactionTemplate.execute(status ->
                    transactionRepository.findByIdempotencyKey(idempotencyKey).orElseGet(action));
        } catch (DataIntegrityViolationException ex) {
            return transactionRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> ex);
        }
    }

    private Transaction apply(
            UUID userId,
            BigDecimal amount,
            EntryType type,
            TransactionReason reason,
            String idempotencyKey
    ) {
        Wallet wallet = walletRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        return transactionRepository.findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> persist(wallet, amount, type, reason, idempotencyKey));
    }

    private Transaction persist(
            Wallet wallet,
            BigDecimal amount,
            EntryType type,
            TransactionReason reason,
            String idempotencyKey
    ) {
        if (type == EntryType.CREDIT) {
            wallet.credit(amount);
        } else {
            wallet.debit(amount);
        }

        walletRepository.save(wallet);
        return transactionRepository.saveAndFlush(Transaction.record(
                wallet.getUserId(),
                type,
                amount,
                wallet.getBalance(),
                reason,
                idempotencyKey
        ));
    }
}
