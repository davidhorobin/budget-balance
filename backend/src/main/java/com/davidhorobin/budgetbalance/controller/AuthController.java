package com.davidhorobin.budgetbalance.controller;

import com.davidhorobin.budgetbalance.dto.auth.LoginRequest;
import com.davidhorobin.budgetbalance.dto.auth.LoginResponse;
import com.davidhorobin.budgetbalance.dto.auth.RefreshResponse;
import com.davidhorobin.budgetbalance.dto.auth.RegisterRequest;
import com.davidhorobin.budgetbalance.service.AuthService;
import com.davidhorobin.budgetbalance.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.verify(request);
        ResponseCookie refresh = buildRefreshCookie(response.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refresh.toString())
                .body(Map.of("accessToken", response.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        RefreshResponse response = refreshTokenService.verify(refreshToken);
        if (!response.success()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        ResponseCookie refresh = buildRefreshCookie(response.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refresh.toString())
                .body(Map.of("accessToken", response.accessToken()));
    }

    private ResponseCookie buildRefreshCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .sameSite("Strict")
                .path("/auth/refresh")
                .maxAge(Duration.ofDays(14))
                .build();
    }

}
