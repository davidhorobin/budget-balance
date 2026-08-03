package com.davidhorobin.budgetbalance.mapper;

import com.davidhorobin.budgetbalance.dto.auth.LoginRequest;
import com.davidhorobin.budgetbalance.dto.auth.RegisterRequest;
import com.davidhorobin.budgetbalance.entity.User;

public class AuthMapper {

    public static User toEntity(RegisterRequest request) {
        User u = new User();
        u.setUsername(request.username());
        u.setPassword(request.password());
        u.setEmail(request.email());
        return u;
    }

    public static User toEntity(LoginRequest request) {
        User u = new User();
        u.setUsername(request.username());
        u.setPassword(request.password());
        return u;
    }

}
