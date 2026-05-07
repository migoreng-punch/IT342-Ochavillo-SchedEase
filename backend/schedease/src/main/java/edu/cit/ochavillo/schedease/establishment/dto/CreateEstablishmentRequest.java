package edu.cit.ochavillo.schedease.establishment.dto;

public record CreateEstablishmentRequest(
        String name,
        String description,
        String address,
        String contactEmail,
        Integer slotDurationMinutes
) {}
