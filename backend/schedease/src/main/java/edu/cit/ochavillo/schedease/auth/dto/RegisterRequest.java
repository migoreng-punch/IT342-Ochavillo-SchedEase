package edu.cit.ochavillo.schedease.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.cit.ochavillo.schedease.user.enums.UserRoles;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, message = "Username must be at least 3 characters")
        @JsonProperty("username")
        String username,

        @NotBlank(message = "First name is required")
        @JsonProperty("firstName")
        String firstName,

        @NotBlank(message = "Last name is required")
        @JsonProperty("lastName")
        String lastName,

        // 🚨 NEW: Phone Number Field
        @NotBlank(message = "Phone number is required")
        @Size(min = 7, message = "Phone number must be at least 7 characters")
        @JsonProperty("phoneNumber")
        String phoneNumber,

        // 🚨 NEW: Address Field
        @NotBlank(message = "Address is required")
        @Size(min = 5, message = "Address must be at least 5 characters")
        @JsonProperty("address")
        String address,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @JsonProperty("email")
        String email,

        @NotNull(message = "Role is required")
        @JsonProperty("role")
        UserRoles role,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        @JsonProperty("password")
        String password
) {}