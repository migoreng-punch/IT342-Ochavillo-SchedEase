package edu.cit.ochavillo.schedease.service;

import edu.cit.ochavillo.schedease.repository.UserRepository;
import edu.cit.ochavillo.schedease.repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
public class MaintenanceService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    public MaintenanceService(UserRepository userRepository,
                              VerificationTokenRepository verificationTokenRepository) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @Scheduled(cron = "0 0 3 * * *") // every day at 3AM "0 0 3 * * *"
    public void deleteUnverifiedUsers() {
        Instant cutoff = Instant.now().minus(30, ChronoUnit.DAYS);
        userRepository.deleteByEnabledFalseAndCreatedAtBefore(cutoff);
    }

    @Scheduled(cron = "0 0 * * * *") // every hour "0 0 * * * *"
    public void cleanExpiredTokens() {
        System.out.println("Running token cleanup...");
        verificationTokenRepository.deleteByExpiryDateBefore(Instant.now());
    }
}
