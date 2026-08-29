package com.altech.walletledger.controller;

import com.altech.walletledger.constant.AppConstants;
import com.altech.walletledger.dto.request.PurchaseRequest;
import com.altech.walletledger.dto.request.TransferRequest;
import com.altech.walletledger.dto.response.ApiResponse;
import com.altech.walletledger.dto.response.PurchaseResponse;
import com.altech.walletledger.dto.response.TransferResponse;
import com.altech.walletledger.dto.response.TransactionHistoryResponse;
import com.altech.walletledger.dto.response.WalletResponse;
import com.altech.walletledger.entity.Purchase;
import com.altech.walletledger.mapper.WalletMapper;
import com.altech.walletledger.service.PurchaseService;
import com.altech.walletledger.service.WalletService;
import com.altech.walletledger.util.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequestMapping(AppConstants.WALLET_ME_BASE)
@Tag(name = "Wallets")
@SecurityRequirement(name = AppConstants.JWT_SCHEME)
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final PurchaseService purchaseService;
    private final WalletMapper walletMapper;

    @PostMapping("/purchases")
    @Operation(summary = "Start a mocked payment (pending until payment webhook)")
    public ApiResponse<PurchaseResponse> purchase(
            @RequestHeader(AppConstants.IDEMPOTENCY_KEY_HEADER)
            @NotBlank
            @Size(max = AppConstants.IDEMPOTENCY_KEY_MAX_LENGTH)
            String idempotencyKey,
            @Valid @RequestBody PurchaseRequest request
    ) {
        UUID userId = AuthUser.id();
        Purchase purchase = purchaseService.create(userId, request.amount(), idempotencyKey);
        return ApiResponse.ok(walletMapper.toPurchaseResponse(purchase, walletService.getWallet(userId).getBalance()));
    }

    @PostMapping("/transfers")
    @Operation(summary = "Transfer money to another user")
    public ApiResponse<TransferResponse> transfer(
            @RequestHeader(AppConstants.IDEMPOTENCY_KEY_HEADER)
            @NotBlank
            @Size(max = AppConstants.IDEMPOTENCY_KEY_MAX_LENGTH)
            String idempotencyKey,
            @Valid @RequestBody TransferRequest request
    ) {
        return ApiResponse.ok(walletMapper.toTransferResponse(walletService.transfer(
                AuthUser.id(),
                request.recipientEmail(),
                request.amount(),
                idempotencyKey
        )));
    }

    @GetMapping
    @Operation(summary = "Get the authenticated user's balance")
    public ApiResponse<WalletResponse> getBalance() {
        return ApiResponse.ok(walletMapper.toResponse(walletService.getWallet(AuthUser.id())));
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get the authenticated user's transaction history")
    public ApiResponse<TransactionHistoryResponse> getHistory(
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE) @Min(0) int page,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) @Min(1) @Max(AppConstants.MAX_PAGE_SIZE) int size
    ) {
        UUID userId = AuthUser.id();
        return ApiResponse.ok(walletMapper.toHistory(userId, walletService.getHistory(userId, page, size)));
    }
}
