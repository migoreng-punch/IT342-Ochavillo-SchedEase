package edu.cit.ochavillo.schedease.auth.repository;

import edu.cit.ochavillo.schedease.user.entity.User;
import edu.cit.ochavillo.schedease.auth.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository
        extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByToken(String token);

    void deleteByExpiryDateBefore(Instant expiryDate);

    Optional<VerificationToken>
    findByUserAndUsedFalse(User user);
}