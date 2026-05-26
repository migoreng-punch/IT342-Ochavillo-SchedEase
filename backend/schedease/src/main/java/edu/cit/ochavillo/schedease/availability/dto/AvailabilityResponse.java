package edu.cit.ochavillo.schedease.availability.dto;

import java.time.LocalTime;

public record AvailabilityResponse(
        String dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {}