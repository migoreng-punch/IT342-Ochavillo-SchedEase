package edu.cit.ochavillo.schedease.repository;

import edu.cit.ochavillo.schedease.entity.User;
import edu.cit.ochavillo.schedease.entity.VerificationToken;
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