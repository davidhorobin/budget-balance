package com.davidhorobin.budgetbalance.repository;

import com.davidhorobin.budgetbalance.entity.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query(value = "UPDATE refresh_tokens SET revoked = true WHERE user_id = :userId", nativeQuery = true)
    int revokeAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE refresh_tokens SET revoked = true WHERE user_id = :userId AND NOT hash = :hash", nativeQuery = true)
    int revokeAllExceptCurrent(@Param("userId") Long userId, @Param("hash") String hash);
}
