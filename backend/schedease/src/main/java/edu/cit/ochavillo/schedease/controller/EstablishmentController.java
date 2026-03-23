package edu.cit.ochavillo.schedease.controller;

import edu.cit.ochavillo.schedease.dto.CreateEstablishmentRequest;
import edu.cit.ochavillo.schedease.dto.UpdateEstablishmentRequest;
import edu.cit.ochavillo.schedease.entity.Establishment;
import edu.cit.ochavillo.schedease.entity.User;
import edu.cit.ochavillo.schedease.repository.EstablishmentRepository;
import edu.cit.ochavillo.schedease.repository.UserRepository;
import edu.cit.ochavillo.schedease.service.AvailabilityService;
import edu.cit.ochavillo.schedease.service.EstablishmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/establishments")
public class EstablishmentController {

    private final AvailabilityService availabilityService;
    private final EstablishmentRepository establishmentRepository;
    private final EstablishmentService establishmentService;
    private final UserRepository userRepository;

    public EstablishmentController(AvailabilityService availabilityService,
                                   EstablishmentRepository establishmentRepository,
                                   EstablishmentService establishmentService,
                                   UserRepository userRepository) {

        this.availabilityService = availabilityService;
        this.establishmentRepository = establishmentRepository;
        this.establishmentService = establishmentService;
        this.userRepository = userRepository;
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

    @GetMapping
    public ResponseEntity<?> getAllEstablishments(
            @RequestParam(required = false) String search) {

        return ResponseEntity.ok(
                establishmentService.getEstablishments(search)
        );
    }

    // 🔎 Get Establishment Details
    @GetMapping("/{id}")
    public ResponseEntity<?> getEstablishment(@PathVariable Long id) {

        return ResponseEntity.ok(
                establishmentService.getEstablishmentById(id)
        );
    }

    // 🏢 Create Establishment (Provider)
    @PostMapping
    public ResponseEntity<?> createEstablishment(
            @AuthenticationPrincipal String username,
            @RequestBody CreateEstablishmentRequest request) {

        User provider = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                establishmentService.createEstablishment(provider, request)
        );
    }

    // ✏️ Update Establishment
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEstablishment(
            @PathVariable Long id,
            @AuthenticationPrincipal String username,
            @RequestBody UpdateEstablishmentRequest request) {

        User provider = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        establishmentService.updateEstablishment(id, provider, request);

        return ResponseEntity.ok("Establishment updated.");
    }
}
