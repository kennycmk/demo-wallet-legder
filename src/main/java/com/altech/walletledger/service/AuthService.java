package com.altech.walletledger.service;

import com.altech.walletledger.dto.response.AuthResponse;
import com.altech.walletledger.dto.response.RegisterResponse;
import com.altech.walletledger.entity.User;
import com.altech.walletledger.entity.Wallet;
import com.altech.walletledger.exception.EmailAlreadyRegisteredException;
import com.altech.walletledger.exception.InvalidCredentialsException;
import com.altech.walletledger.repository.UserRepository;
import com.altech.walletledger.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public RegisterResponse register(String email, String password) {
        String normalized = email.toLowerCase();
        if (userRepository.existsByEmail(normalized)) {
            throw new EmailAlreadyRegisteredException(normalized);
        }
        User user = userRepository.save(User.register(normalized, passwordEncoder.encode(password)));
        walletRepository.save(Wallet.open(user.getId()));
        return new RegisterResponse(user.getId(), user.getEmail());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return new AuthResponse(user.getId(), user.getEmail(), jwtService.createToken(user.getId(), user.getEmail()));
    }
}
