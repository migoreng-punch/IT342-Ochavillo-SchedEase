package edu.cit.ochavillo.schedease.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record RescheduleRequest(
        LocalDate date,
        LocalTime startTime
) {}