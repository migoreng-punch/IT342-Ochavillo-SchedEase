package edu.cit.ochavillo.schedease.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {}
