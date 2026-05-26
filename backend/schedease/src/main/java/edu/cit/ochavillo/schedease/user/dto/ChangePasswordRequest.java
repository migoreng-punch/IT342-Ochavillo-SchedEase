package edu.cit.ochavillo.schedease.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required")
        @JsonProperty("currentPassword")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        @JsonProperty("newPassword")
        String newPassword,

        @NotBlank(message = "Please confirm your new password")
        @JsonProperty("confirmPassword")
        String confirmPassword
) {}