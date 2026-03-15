package edu.cit.ochavillo.schedease.dto;

public record CreateEstablishmentRequest(
        String name,
        String description,
        String address,
        String contactEmail
) {}
