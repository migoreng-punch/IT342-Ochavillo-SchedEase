package edu.cit.ochavillo.schedease.appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record BookAppointmentRequest(
        Long establishmentId,
        LocalDate date,
        LocalTime startTime
) { }
