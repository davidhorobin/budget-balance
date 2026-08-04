package com.davidhorobin.budgetbalance.service;

import com.davidhorobin.budgetbalance.dto.auth.RefreshRequest;
import com.davidhorobin.budgetbalance.dto.auth.RefreshResponse;
import com.davidhorobin.budgetbalance.entity.RefreshToken;
import com.davidhorobin.budgetbalance.entity.User;
import com.davidhorobin.budgetbalance.repository.RefreshTokenRepo;
import com.davidhorobin.budgetbalance.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private static final long EXPIRATION_TIME = 60 * 60 * 24 * 14;

    private final RefreshTokenRepo refreshTokenRepo;
    private final JwtService jwtService;

    public String create(User user) {
        String token = UUID.randomUUID().toString();
        String hashedToken = DigestUtils.sha256Hex(token);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(hashedToken);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(EXPIRATION_TIME));
        refreshToken.setUser(user);

        refreshTokenRepo.save(refreshToken);

        return token;
    }

    public RefreshResponse verify(RefreshRequest request) {
        Optional<RefreshToken> saved = refreshTokenRepo.findByToken(DigestUtils.sha256Hex(request.token()));
        if (saved.isEmpty()) {
            log.error(DigestUtils.sha256Hex(request.token()));
            log.error(DigestUtils.sha256Hex(request.token()));
            log.error("Refresh token not found");
            return new RefreshResponse(false);
        }
        RefreshToken token = saved.get();
        User user = token.getUser();
        if (token.isRevoked()) {
            log.error("Token has been revoked");
            refreshTokenRepo.revokeAllByUserId(user.getId());
            return new RefreshResponse(false);
        }
        if (token.getExpiryDate().isBefore(Instant.now())) {
            log.error("Token has expired");
            return new RefreshResponse(false);
        }

        refreshTokenRepo.revokeAllByUserId(user.getId());
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = create(user);
        return new RefreshResponse(true, newAccessToken, newRefreshToken);

    }

}
