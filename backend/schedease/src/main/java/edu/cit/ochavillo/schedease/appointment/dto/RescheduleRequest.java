package edu.cit.ochavillo.schedease.appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record RescheduleRequest(
        LocalDate date,
        LocalTime startTime
) {}