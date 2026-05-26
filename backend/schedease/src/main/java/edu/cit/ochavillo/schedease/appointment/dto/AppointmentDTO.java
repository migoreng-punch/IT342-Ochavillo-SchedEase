package edu.cit.ochavillo.schedease.appointment.dto;

import edu.cit.ochavillo.schedease.appointment.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentDTO(
        UUID id,
        Long clientId,
        String clientName,
        Long establishmentId,
        String establishmentName,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentStatus status
) {

}
