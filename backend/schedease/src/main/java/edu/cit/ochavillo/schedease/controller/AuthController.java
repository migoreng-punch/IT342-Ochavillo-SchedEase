package edu.cit.ochavillo.schedease.controller;

import edu.cit.ochavillo.schedease.dto.LoginRequest;
import edu.cit.ochavillo.schedease.dto.LoginResponse;
import edu.cit.ochavillo.schedease.dto.RegisterRequest;
import edu.cit.ochavillo.schedease.dto.RegisterResponse;
import edu.cit.ochavillo.schedease.service.AuthService;
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

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new RegisterResponse("User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ip = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        LoginResponse response = authService.login(request, ip, userAgent);

        ResponseCookie refreshCookie = ResponseCookie.from(
                        "refreshToken", response.refreshToken())
                .httpOnly(true)
                .secure(true)        // set false only in local dev if needed
                .path("/api/auth")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                // return ONLY access token to client
                .body(new LoginResponse(response.accessToken(), null));
    }


    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletRequest httpRequest) {

        String ip = httpRequest.getRemoteAddr();
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
    public ResponseEntity<?> verify(@RequestParam String token) {

        authService.verify(token);

        return ResponseEntity.ok("Account verified successfully");
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(
            @RequestParam String email) {

        authService.resendVerification(email);

        return ResponseEntity.ok("Verification email sent");
    }

}
