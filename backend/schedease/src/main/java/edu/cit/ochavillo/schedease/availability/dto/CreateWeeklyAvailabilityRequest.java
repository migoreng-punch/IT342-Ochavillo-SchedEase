package edu.cit.ochavillo.schedease.availability.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record CreateWeeklyAvailabilityRequest(
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) { }