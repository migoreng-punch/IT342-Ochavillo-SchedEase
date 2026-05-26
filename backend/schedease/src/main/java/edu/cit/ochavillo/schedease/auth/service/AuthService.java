package edu.cit.ochavillo.schedease.auth.service;

import edu.cit.ochavillo.schedease.auth.dto.LoginRequest;
import edu.cit.ochavillo.schedease.auth.dto.LoginResponse;
import edu.cit.ochavillo.schedease.auth.dto.RegisterRequest;
import edu.cit.ochavillo.schedease.user.entity.User;
import edu.cit.ochavillo.schedease.auth.entity.VerificationToken;
import edu.cit.ochavillo.schedease.user.enums.UserRoles;
import edu.cit.ochavillo.schedease.user.repository.UserRepository;
import edu.cit.ochavillo.schedease.auth.repository.VerificationTokenRepository;
import edu.cit.ochavillo.schedease.security.JwtUtil;
import edu.cit.ochavillo.schedease.util.EmailService;
import edu.cit.ochavillo.schedease.auth.service.RefreshTokenService;
import edu.cit.ochavillo.schedease.util.AppException;
import edu.cit.ochavillo.schedease.util.ExpiredTokenException;
import org.springframework.scheduling.annotation.Async;
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

    public User register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.email())) {
            throw new AppException("AUTH-004", "Username already exists");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new AppException("AUTH-004", "Email already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setAddress(request.address());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setEnabled(false);

        userRepository.save(user);

        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(Instant.now().plus(5, ChronoUnit.MINUTES));

        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), token);

        return user;
    }


    public LoginResponse login(LoginRequest request, String ip, String userAgent) {

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new AppException("AUTH-001", "Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AppException("AUTH-001", "Invalid username or password");
        }

//        if (!user.isEnabled()) {
//            throw new AppException("USER-003", "Email not verified. Please verify your email.");
//        }

        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getFirstName(), user.getRole(), user.isEmailVerified());
        String refreshToken = refreshTokenService.create(user, ip, userAgent);

        return new LoginResponse(accessToken, refreshToken);
    }

    public LoginResponse refresh(String refreshToken, String ip, String userAgent) {

        String newRefreshToken = refreshTokenService.rotate(refreshToken, ip, userAgent);

        User user = refreshTokenService.validate(newRefreshToken);

        String newAccessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getFirstName(), user.getRole(), user.isEmailVerified());

        return new LoginResponse(newAccessToken, newRefreshToken);
    }

    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    public void logoutAll(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("USER-001", "User not found"));

        refreshTokenService.revokeAll(user);
    }

    public String getUsernameFromRefresh(String rawToken) {
        User user = refreshTokenService.validate(rawToken);
        return user.getUsername();
    }

    public void verify(String token) {

        VerificationToken vt = verificationTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new AppException("VERIFY-001", "Invalid verification token"));

        if (Instant.now().isAfter(vt.getExpiryDate())) {
            String userEmail = vt.getUser().getEmail();
            throw new ExpiredTokenException("Verification link has expired.", userEmail);
        }

        User user = vt.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationTokenRepository.delete(vt);
    }

    @Async
    public void resendVerification(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("USER-001", "User not found"));

        if (user.isEmailVerified()) {
            throw new AppException("VERIFY-005", "Account already verified");
        }

        // 🔒 Rate limit (60 seconds)
        if (user.getLastVerificationSentAt() != null &&
                user.getLastVerificationSentAt()
                        .isAfter(Instant.now().minusSeconds(60))) {

            throw new AppException(
                    "VERIFY-004", "Please wait before requesting another verification email."
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
