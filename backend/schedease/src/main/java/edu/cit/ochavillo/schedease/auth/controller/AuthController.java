package edu.cit.ochavillo.schedease.auth.controller;

import edu.cit.ochavillo.schedease.auth.dto.LoginRequest;
import edu.cit.ochavillo.schedease.auth.dto.LoginResponse;
import edu.cit.ochavillo.schedease.auth.dto.RegisterRequest;
import edu.cit.ochavillo.schedease.auth.dto.RegisterResponse;
import edu.cit.ochavillo.schedease.security.RateLimitPlan;
import edu.cit.ochavillo.schedease.auth.service.AuthService;
import edu.cit.ochavillo.schedease.security.RateLimitingService;
import edu.cit.ochavillo.schedease.util.ApiErrorResponse;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RateLimitingService rateLimiter;

    public AuthController(AuthService authService, RateLimitingService rateLimiter) {
        this.authService = authService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) { // 🚨 Added httpRequest to get the IP

        String ip = httpRequest.getRemoteAddr();

        // 🚨 1. RATE LIMIT CHECK: Stop bot account creation
        Bucket bucket = rateLimiter.resolveBucket(ip, RateLimitPlan.AUTH);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ApiErrorResponse("429","Too many registration attempts. Please wait 1 minute."));
        }

        // 2. Proceed with registration
        authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new RegisterResponse("User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ip = httpRequest.getRemoteAddr();

        // 🚨 1. RATE LIMIT CHECK: Bounce them immediately if they are spamming
        Bucket bucket = rateLimiter.resolveBucket(ip, RateLimitPlan.AUTH);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ApiErrorResponse("429","Too many login attempts. Please wait 1 minute."));
        }

        // 2. Proceed with secure login
        String userAgent = httpRequest.getHeader("User-Agent");
        LoginResponse response = authService.login(request, ip, userAgent);

        // 3. Build the secure HttpOnly cookie
        ResponseCookie refreshCookie = ResponseCookie.from(
                        "refreshToken", response.refreshToken())
                .httpOnly(true)
                .secure(true)        // set false only in local dev if needed
                .path("/api/auth")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .sameSite("Strict")
                .build();

        // 4. Return Access Token in body, Refresh Token in cookie
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new LoginResponse(response.accessToken(), null));
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletRequest httpRequest) {

        String ip = httpRequest.getRemoteAddr();

        Bucket bucket = rateLimiter.resolveBucket(ip, RateLimitPlan.AUTH);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ApiErrorResponse("429","Too many request attempts. Please wait 1 minute."));
        }

        String userAgent = httpRequest.getHeader("User-Agent");
        LoginResponse response = authService.refresh(refreshToken, ip, userAgent);

        ResponseCookie refreshCookie = ResponseCookie.from(
                        "refreshToken", response.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/api/auth")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new LoginResponse(response.accessToken(), null));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        System.out.println("Logout received refresh token: " + refreshToken);

        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/api/auth")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body("Logged out successfully");
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(
            @CookieValue("refreshToken") String refreshToken) {

        String username = authService.getUsernameFromRefresh(refreshToken);
        authService.logoutAll(username);

        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/api/auth")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body("Logged out from all devices");
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String token,
                                    HttpServletRequest httpRequest) {

        String ip = httpRequest.getRemoteAddr();

        Bucket bucket = rateLimiter.resolveBucket(ip, RateLimitPlan.AUTH);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ApiErrorResponse("429","Too many login attempts. Please wait 1 minute."));
        }
        authService.verify(token);

        return ResponseEntity.ok("Account verified successfully");
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(
            @RequestParam String email,
            HttpServletRequest httpRequest) {

        String ip = httpRequest.getRemoteAddr();

        Bucket bucket = rateLimiter.resolveBucket(ip, RateLimitPlan.AUTH);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ApiErrorResponse("429","Too many login attempts. Please wait 1 minute."));
        }

        authService.resendVerification(email);

        return ResponseEntity.ok("Verification email sent");
    }

}
