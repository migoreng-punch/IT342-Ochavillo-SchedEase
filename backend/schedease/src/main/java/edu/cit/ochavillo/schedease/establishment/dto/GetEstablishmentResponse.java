package edu.cit.ochavillo.schedease.establishment.dto;

public record GetEstablishmentResponse(
        Long id,
        String name,
        String description,
        String address,
        String contactEmail,
        String providerName
) {
}
