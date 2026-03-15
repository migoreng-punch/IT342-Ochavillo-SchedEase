package edu.cit.ochavillo.schedease.dto;

public record UpdateEstablishmentRequest(
        String name,
        String description,
        String address,
        String contactEmail
) {}