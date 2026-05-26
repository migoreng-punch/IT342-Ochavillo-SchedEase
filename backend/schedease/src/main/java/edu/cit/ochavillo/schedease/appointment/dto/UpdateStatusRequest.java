package edu.cit.ochavillo.schedease.appointment.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateStatusRequest(
        @NotBlank(message = "Status cannot be blank")
        String status
) {}