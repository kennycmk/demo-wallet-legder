package com.altech.walletledger.controller;

import com.altech.walletledger.constant.AppConstants;
import com.altech.walletledger.dto.request.PaymentWebhookRequest;
import com.altech.walletledger.dto.response.ApiResponse;
import com.altech.walletledger.dto.response.PurchaseResponse;
import com.altech.walletledger.entity.Purchase;
import com.altech.walletledger.mapper.WalletMapper;
import com.altech.walletledger.service.PurchaseService;
import com.altech.walletledger.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstants.WEBHOOK_BASE)
@Tag(name = "Webhooks")
@SecurityRequirements
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PurchaseService purchaseService;
    private final WalletService walletService;
    private final WalletMapper walletMapper;

    @PostMapping("/payments")
    @Operation(summary = "Payment gateway callback (mock). Credits wallet only when payment succeeded.")
    public ApiResponse<PurchaseResponse> handlePayment(@Valid @RequestBody PaymentWebhookRequest request) {
        Purchase purchase = purchaseService.handlePayment(request.purchaseId(), request.event());
        return ApiResponse.ok(walletMapper.toPurchaseResponse(
                purchase,
                walletService.getWallet(purchase.getUserId()).getBalance()
        ));
    }
}
