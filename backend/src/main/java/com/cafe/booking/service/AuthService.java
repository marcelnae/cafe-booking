package com.cafe.booking.service;

import com.cafe.booking.dto.Dtos;
import com.cafe.booking.entity.Waiter;
import com.cafe.booking.repository.WaiterRepository;
import com.cafe.booking.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final WaiterRepository waiterRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long expirationMs;

    public AuthService(WaiterRepository waiterRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.waiterRepository = waiterRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.expirationMs = expirationMs;
    }

    public Dtos.LoginResponse login(Dtos.LoginRequest request) {
        Waiter waiter = waiterRepository.findByNameIgnoreCase(request.name())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!waiter.isActive() || !passwordEncoder.matches(request.pin(), waiter.getPinHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtService.generate(waiter.getId(), waiter.getName());
        return new Dtos.LoginResponse(token, waiter.getId(), waiter.getName(), expirationMs);
    }
}
