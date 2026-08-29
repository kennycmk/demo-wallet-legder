package com.altech.walletledger.mapper;

import com.altech.walletledger.dto.response.PurchaseResponse;
import com.altech.walletledger.dto.response.TransferResponse;
import com.altech.walletledger.dto.response.TransactionHistoryResponse;
import com.altech.walletledger.dto.response.TransactionResponse;
import com.altech.walletledger.dto.response.WalletResponse;
import com.altech.walletledger.entity.Purchase;
import com.altech.walletledger.entity.Transaction;
import com.altech.walletledger.entity.Wallet;
import com.altech.walletledger.service.WalletService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mapping(source = "entryType", target = "type")
    TransactionResponse toTransactionResponse(Transaction transaction);

    @Mapping(target = "transaction", ignore = true)
    WalletResponse toResponse(Wallet wallet);

    default WalletResponse toResponse(Wallet wallet, Transaction transaction) {
        return new WalletResponse(
                wallet.getUserId(),
                wallet.getBalance(),
                wallet.getUpdatedAt(),
                toTransactionResponse(transaction)
        );
    }

    default TransactionHistoryResponse toHistory(UUID userId, Page<Transaction> page) {
        return new TransactionHistoryResponse(
                userId,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getContent().stream().map(this::toTransactionResponse).toList()
        );
    }

    default PurchaseResponse toPurchaseResponse(Purchase purchase, java.math.BigDecimal walletBalance) {
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getAmount(),
                purchase.getStatus(),
                walletBalance,
                purchase.getCreatedAt()
        );
    }

    default TransferResponse toTransferResponse(WalletService.TransferResult result) {
        return new TransferResponse(
                result.debit().getUserId(),
                result.credit().getUserId(),
                result.debit().getAmount(),
                result.sender().getBalance(),
                result.debit().getCreatedAt()
        );
    }
}
