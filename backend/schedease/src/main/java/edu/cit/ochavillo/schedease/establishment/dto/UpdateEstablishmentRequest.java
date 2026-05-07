package edu.cit.ochavillo.schedease.establishment.dto;

public record UpdateEstablishmentRequest(
        String name,
        String description,
        String address,
        String contactEmail,
        Integer slotDurationMinutes,
        Integer bufferMinutes,
        Integer bookingCutoffHours
) {}