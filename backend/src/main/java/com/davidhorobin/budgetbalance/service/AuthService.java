package com.davidhorobin.budgetbalance.service;

import com.davidhorobin.budgetbalance.dto.auth.*;
import com.davidhorobin.budgetbalance.entity.RefreshToken;
import com.davidhorobin.budgetbalance.exception.InvalidCredentialsException;
import com.davidhorobin.budgetbalance.security.jwt.JwtService;
import com.davidhorobin.budgetbalance.entity.User;
import com.davidhorobin.budgetbalance.mapper.AuthMapper;
import com.davidhorobin.budgetbalance.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepo userRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public RegisterResponse register(RegisterRequest registerRequest) {
        User user = AuthMapper.toEntity(registerRequest);
        user.setPassword(encoder.encode(user.getPassword()));
        Optional<User> check = userRepo.findByUsername(user.getUsername());
        if (check.isPresent())
            return new RegisterResponse(false);
        userRepo.save(user);
        return new RegisterResponse(true);
    }


    public LoginResponse verify(LoginRequest loginRequest) {
        User user = userRepo.findByUsername(loginRequest.username()).orElseThrow(
                () -> new InvalidCredentialsException("Invalid username or password")
        );
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
            );
        } catch (AuthenticationException e) {
            log.error(e.getMessage());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.create(user);

        refreshTokenService.invalidateOtherTokens(refreshToken);

        return new LoginResponse(accessToken, refreshToken);
    }

}
