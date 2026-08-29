package com.altech.walletledger.controller;

import com.altech.walletledger.constant.AppConstants;
import com.altech.walletledger.dto.request.AuthRequest;
import com.altech.walletledger.dto.response.ApiResponse;
import com.altech.walletledger.dto.response.AuthResponse;
import com.altech.walletledger.dto.response.RegisterResponse;
import com.altech.walletledger.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstants.AUTH_BASE)
@Tag(name = "Auth")
@SecurityRequirements
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a user and open a wallet")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody AuthRequest request) {
        return ApiResponse.ok(authService.register(request.email(), request.password()));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive a JWT")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ApiResponse.ok(authService.login(request.email(), request.password()));
    }
}
