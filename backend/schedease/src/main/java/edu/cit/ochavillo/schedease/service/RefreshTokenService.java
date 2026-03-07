package edu.cit.ochavillo.schedease.service;

import edu.cit.ochavillo.schedease.entity.RefreshToken;
import edu.cit.ochavillo.schedease.entity.User;
import edu.cit.ochavillo.schedease.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    public RefreshTokenService(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    public String create(User user, String ip, String userAgent) {
        String rawToken = generateToken();
        String hash = hash(rawToken);

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(hash);
        rt.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        rt.setIpAddress(ip);
        rt.setUserAgent(userAgent);

        repository.save(rt);
        return rawToken;
    }

    public User validate(String rawToken) {
        String hash = hash(rawToken);

        RefreshToken rt = repository.findByTokenHashAndRevokedFalse(hash)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (rt.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        return rt.getUser();
    }

    private String generateToken() {
        return UUID.randomUUID() + "." + UUID.randomUUID();
    }

    private String hash(String token) {
        return DigestUtils.sha256Hex(token);
    }

    public void revoke(String rawToken) {
        String hash = hash(rawToken);

        System.out.println("Raw token: " + rawToken);
        System.out.println("Hashed token: " + hash);

        repository.findByTokenHashAndRevokedFalse(hash)
                .ifPresentOrElse(rt -> {
                            System.out.println("Token found. Revoking...");
                            rt.setRevoked(true);
                            repository.save(rt);
                        }, () -> System.out.println("Token NOT FOUND in DB")
                );
    }

    public String rotate(String rawToken, String ip, String userAgent) {
        String hash = hash(rawToken);

        RefreshToken existing = repository.findByTokenHashAndRevokedFalse(hash)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (existing.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        // 🚨 Reuse detection
        // Can remove
        if (existing.isRevoked()) {
            throw new RuntimeException("Refresh token reuse detected");
        }

        // 🔄 Revoke old token
        existing.setRevoked(true);
        repository.save(existing);

        // 🆕 Issue new token
        return create(existing.getUser(), ip, userAgent);
    }

    @Transactional
    public void revokeAll(User user) {
        repository.revokeAllByUser(user);
    }

}
