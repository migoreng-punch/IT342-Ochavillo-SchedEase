package edu.cit.ochavillo.schedease.dto;

import edu.cit.ochavillo.schedease.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentDTO(
        UUID id,
        Long clientId,
        Long establishmentId,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentStatus status
) {

}
