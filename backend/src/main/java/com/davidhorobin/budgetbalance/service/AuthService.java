package com.davidhorobin.budgetbalance.service;

import com.davidhorobin.budgetbalance.dto.auth.LoginResponse;
import com.davidhorobin.budgetbalance.dto.auth.RegisterResponse;
import com.davidhorobin.budgetbalance.dto.auth.LoginRequest;
import com.davidhorobin.budgetbalance.dto.auth.RegisterRequest;
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
        User user = AuthMapper.toEntity(loginRequest);
        Optional<User> stored = userRepo.findByUsername(user.getUsername());
        if (stored.isEmpty())
            return new LoginResponse(null);
        user.setEmail(stored.get().getEmail());
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );
            return new LoginResponse(jwtService.generateToken(user.getUsername()));
        } catch (AuthenticationException e) {
            return new LoginResponse(null);
        }
    }
}
