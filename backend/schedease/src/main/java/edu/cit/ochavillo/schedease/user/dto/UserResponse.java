package edu.cit.ochavillo.schedease.user.dto;

import java.time.Instant;

public record UserResponse(
        String username,
        String firstName,
        String lastName,
        String phoneNumber,
        String address,
        Boolean enabled,
        String email,
        String role,
        Instant createdAt
) {}
