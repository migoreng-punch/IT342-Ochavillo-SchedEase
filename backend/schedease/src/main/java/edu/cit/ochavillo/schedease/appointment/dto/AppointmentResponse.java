package edu.cit.ochavillo.schedease.appointment.dto;

public record AppointmentResponse(
        String message,
        AppointmentDTO data
) {
}
