package edu.cit.ochavillo.schedease.availability.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AvailabilityOverrideRequest(
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        boolean unavailable
) {}