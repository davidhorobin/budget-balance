package com.davidhorobin.budgetbalance.service;

import com.davidhorobin.budgetbalance.dto.auth.LoginRequest;
import com.davidhorobin.budgetbalance.dto.auth.LoginResponse;
import com.davidhorobin.budgetbalance.dto.auth.RegisterRequest;
import com.davidhorobin.budgetbalance.entity.User;
import com.davidhorobin.budgetbalance.exception.InvalidCredentialsException;
import com.davidhorobin.budgetbalance.repository.UserRepo;
import com.davidhorobin.budgetbalance.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepo userRepo;
    @Mock
    AuthenticationManager authManager;
    @Mock
    JwtService jwtService;
    @Mock
    RefreshTokenService refreshTokenService;

    AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(userRepo, authManager, jwtService, refreshTokenService);
    }

    @Test
    void registerStoresABcryptHashNotThePassword() {
        when(userRepo.findByUsername("alice")).thenReturn(Optional.empty());

        service.register(new RegisterRequest("alice", "s3cret!", "alice@example.com"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(saved.capture());
        assertThat(saved.getValue().getUsername()).isEqualTo("alice");
        assertThat(saved.getValue().getEmail()).isEqualTo("alice@example.com");
        assertThat(saved.getValue().getPassword()).isNotEqualTo("s3cret!");
        assertThat(new BCryptPasswordEncoder().matches("s3cret!", saved.getValue().getPassword())).isTrue();
    }

    @Test
    void registerSkipsExistingUsername() {
        when(userRepo.findByUsername("alice")).thenReturn(Optional.of(new User()));

        service.register(new RegisterRequest("alice", "s3cret!", "alice@example.com"));

        verify(userRepo, never()).save(any());
    }

    @Test
    void loginWithUnknownUserIsRejected() {
        when(userRepo.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verify(new LoginRequest("ghost", "x")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");
        verifyNoInteractions(jwtService, refreshTokenService);
    }

    @Test
    void loginWithWrongPasswordIsRejected() {
        when(userRepo.findByUsername("alice")).thenReturn(Optional.of(new User()));
        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> service.verify(new LoginRequest("alice", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");
        verifyNoInteractions(jwtService, refreshTokenService);
    }

    @Test
    void successfulLoginIssuesTokensAndEndsOtherSessions() {
        User alice = new User();
        alice.setUsername("alice");
        when(userRepo.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(jwtService.generateAccessToken(alice)).thenReturn("access");
        when(refreshTokenService.create(alice)).thenReturn("refresh");

        LoginResponse response = service.verify(new LoginRequest("alice", "s3cret!"));

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
        verify(refreshTokenService).invalidateOtherTokens("refresh");
    }
}
