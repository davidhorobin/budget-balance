package com.davidhorobin.budgetbalance.service;

import com.davidhorobin.budgetbalance.dto.auth.RegisterRequest;
import com.davidhorobin.budgetbalance.dto.auth.RegisterResponse;
import com.davidhorobin.budgetbalance.entity.User;
import com.davidhorobin.budgetbalance.mapper.AuthMapper;
import com.davidhorobin.budgetbalance.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepo userRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public RegisterResponse register(RegisterRequest registerRequest) {
        User user = AuthMapper.toEntity(registerRequest);
        user.setPassword(encoder.encode(user.getPassword()));
        try {
            User saved = userRepo.save(user);
        } catch (IllegalArgumentException e) {
            return new RegisterResponse(false);
        }
        return new RegisterResponse(true);
    }

}
