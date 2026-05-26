package edu.cit.ochavillo.schedease.user.controller;

import edu.cit.ochavillo.schedease.user.dto.ChangePasswordRequest;
import edu.cit.ochavillo.schedease.user.dto.UpdateUserRequest;
import edu.cit.ochavillo.schedease.user.entity.User;
import edu.cit.ochavillo.schedease.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 🔎 Get My Profile
    @GetMapping
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getUserProfile(user));
    }

    // 🏗 Update My Profile
    @PutMapping
    public ResponseEntity<?> updateMyProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateUserRequest request) {

        return ResponseEntity.ok(userService.updateUserProfile(user, request));
    }

    @PutMapping("/me/password") // Maps to: PUT /api/users/me/password
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(user, request);

        // Return a simple 200 OK with a success message
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }

    // ❌ Delete My Account (Danger Zone)
    @DeleteMapping
    public ResponseEntity<?> deleteMyAccount(@AuthenticationPrincipal User user) {
        userService.deleteUserAccount(user);
        return ResponseEntity.ok("Account successfully deleted.");
    }
}