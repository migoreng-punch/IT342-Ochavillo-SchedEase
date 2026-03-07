package edu.cit.ochavillo.schedease.service;

import edu.cit.ochavillo.schedease.entity.VerificationToken;
import edu.cit.ochavillo.schedease.repository.VerificationTokenRepository;
import edu.cit.ochavillo.schedease.security.JwtUtil;
import edu.cit.ochavillo.schedease.dto.RegisterRequest;
import edu.cit.ochavillo.schedease.entity.User;
import edu.cit.ochavillo.schedease.repository.UserRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       RefreshTokenService refreshTokenService,
                       VerificationTokenRepository verificationTokenRepository,
                       EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
    }

    public void register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setEnabled(false);

        userRepository.save(user);

        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(Instant.now().plus(5, ChronoUnit.MINUTES));

        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), token);
    }


    public LoginResponse login(LoginRequest request, String ip, String userAgent) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        if (!user.isEnabled()) {
            throw new RuntimeException("Email not verified. Please verify your email.");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = refreshTokenService.create(user, ip, userAgent);

        return new LoginResponse(accessToken, refreshToken);
    }

    public LoginResponse refresh(String refreshToken, String ip, String userAgent) {

        String newRefreshToken = refreshTokenService.rotate(refreshToken, ip, userAgent);

        User user = refreshTokenService.validate(newRefreshToken);

        String newAccessToken = jwtUtil.generateAccessToken(user.getUsername());

        return new LoginResponse(newAccessToken, newRefreshToken);
    }

    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    public void logoutAll(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenService.revokeAll(user);
    }

    public String getUsernameFromRefresh(String rawToken) {
        User user = refreshTokenService.validate(rawToken);
        return user.getUsername();
    }

    public void verify(String token) {

        VerificationToken vt = verificationTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (vt.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Verification token expired");
        }

        User user = vt.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationTokenRepository.delete(vt);
    }

    public void resendVerification(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEnabled()) {
            throw new RuntimeException("Account already verified");
        }

        // 🔒 Rate limit (60 seconds)
        if (user.getLastVerificationSentAt() != null &&
                user.getLastVerificationSentAt()
                        .isAfter(Instant.now().minusSeconds(60))) {

            throw new RuntimeException(
                    "Please wait before requesting another verification email."
            );
        }

        verificationTokenRepository
                .findByUserAndUsedFalse(user)
                .ifPresent(existing -> {
                    verificationTokenRepository.delete(existing);
                });

        // Create new verification token
        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(Instant.now().plus(5, ChronoUnit.MINUTES));

        verificationTokenRepository.save(verificationToken);

        // Update resend timestamp
        user.setLastVerificationSentAt(Instant.now());
        userRepository.save(user);

        // Send email (async if already configured)
        emailService.sendVerificationEmail(user.getEmail(), token);
    }
}
