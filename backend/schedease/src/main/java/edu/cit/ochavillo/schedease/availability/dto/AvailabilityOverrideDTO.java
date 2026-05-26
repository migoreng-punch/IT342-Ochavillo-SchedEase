package edu.cit.ochavillo.schedease.availability.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public record AvailabilityOverrideDTO(
        Long id,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate overrideDate,

        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime startTime,

        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime endTime,

        Boolean isUnavailable,
        Long establishmentId
) {
}
