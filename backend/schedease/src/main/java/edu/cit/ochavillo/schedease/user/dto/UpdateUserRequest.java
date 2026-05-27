package edu.cit.ochavillo.schedease.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank(message = "Username cannot be blank")
        String username,

        @NotBlank(message = "first name cannot be blank")
        String firstName,

        @NotBlank(message = "last name cannot be blank")
        String lastName,

        @NotBlank(message = "email cannot be blank")
        String email,

        @NotBlank(message = "phone number cannot be blank")
        String phoneNumber,

        @NotBlank(message = "address cannot be blank")
        String address
) {}