package edu.cit.ochavillo.schedease.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {}
