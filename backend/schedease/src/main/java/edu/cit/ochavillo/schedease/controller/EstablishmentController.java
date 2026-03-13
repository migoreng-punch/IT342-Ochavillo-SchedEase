package edu.cit.ochavillo.schedease.controller;

import edu.cit.ochavillo.schedease.entity.Establishment;
import edu.cit.ochavillo.schedease.repository.EstablishmentRepository;
import edu.cit.ochavillo.schedease.service.AvailabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/establishments")
public class EstablishmentController {

    private final AvailabilityService availabilityService;
    private final EstablishmentRepository establishmentRepository;

    public EstablishmentController(AvailabilityService availabilityService,
                              EstablishmentRepository establishmentRepository) {
        this.availabilityService = availabilityService;
        this.establishmentRepository = establishmentRepository;
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<?> getAvailableSlots(
            @PathVariable Long id,
            @RequestParam LocalDate date) {

        Establishment establishment = establishmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Establishment not found"));

        return ResponseEntity.ok(
                availabilityService.generateAvailableSlots(establishment, date)
        );
    }
}
