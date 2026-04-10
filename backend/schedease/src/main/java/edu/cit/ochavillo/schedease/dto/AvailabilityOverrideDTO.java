package edu.cit.ochavillo.schedease.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AvailabilityOverrideDTO(
        Long id,
        LocalDate overrideDate,
        LocalTime startTime,
        LocalTime endTime,
        Boolean isUnavailable,
        Long establishmentId
) {
}
