package edu.cit.ochavillo.schedease.establishment.dto;

public record EstablishmentDTO(
        Long id,
        String name,
        String description,
        String address,
        String contactEmail,
        Integer slotDurationMinutes,
        Integer bufferMinutes,
        Integer bookingCutoffHours,
        Long providerId,
        String providerName
) {
}
