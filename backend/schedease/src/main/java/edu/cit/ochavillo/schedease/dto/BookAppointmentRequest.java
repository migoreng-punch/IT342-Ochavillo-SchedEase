package edu.cit.ochavillo.schedease.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record BookAppointmentRequest(
        Long establishmentId,
        LocalDate date,
        LocalTime startTime
) { }
