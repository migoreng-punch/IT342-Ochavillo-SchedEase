package edu.cit.ochavillo.schedease.dto;

public record GetEstablishmentResponse(
        Long id,
        String name,
        String description,
        String address,
        String contactEmail,
        String providerName
) {
}
