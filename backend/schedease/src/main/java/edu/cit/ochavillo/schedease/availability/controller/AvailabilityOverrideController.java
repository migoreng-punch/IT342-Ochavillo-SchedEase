package edu.cit.ochavillo.schedease.availability.controller;

import edu.cit.ochavillo.schedease.availability.dto.AvailabilityOverrideDTO;
import edu.cit.ochavillo.schedease.availability.dto.AvailabilityOverrideRequest;
import edu.cit.ochavillo.schedease.availability.dto.AvailabilityOverrideResponse;
import edu.cit.ochavillo.schedease.establishment.entity.Establishment;
import edu.cit.ochavillo.schedease.user.entity.User;
import edu.cit.ochavillo.schedease.user.enums.UserRoles;
import edu.cit.ochavillo.schedease.establishment.repository.EstablishmentRepository;
import edu.cit.ochavillo.schedease.user.repository.UserRepository;
import edu.cit.ochavillo.schedease.availability.service.AvailabilityOverrideService;
import edu.cit.ochavillo.schedease.util.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/availability/overrides")
public class AvailabilityOverrideController {

    private final AvailabilityOverrideService overrideService;
    private final EstablishmentRepository establishmentRepository;
    private final UserRepository userRepository;

    public AvailabilityOverrideController(
            AvailabilityOverrideService overrideService,
            EstablishmentRepository establishmentRepository,
            UserRepository userRepository) {

        this.overrideService = overrideService;
        this.establishmentRepository = establishmentRepository;
        this.userRepository = userRepository;
    }

    // 🏗 Create or Update Override
    @PostMapping
    public ResponseEntity<?> setOverride(
            @AuthenticationPrincipal User provider,
            @RequestBody AvailabilityOverrideRequest request) {

        if((provider.getRole() != UserRoles.PROVIDER)){
            throw new AppException("AUTH-005", "Only Providers can create Overrides");
        }

        Establishment establishment = establishmentRepository
                .findByOwner(provider)
                .orElseThrow(() -> new AppException("ESTAB-001", "Establishment not found"));;

        AvailabilityOverrideDTO override = overrideService.setOverride(
                establishment,
                request.date(),
                request.startTime(),
                request.endTime(),
                request.unavailable()
        );

        AvailabilityOverrideResponse responseBody = new AvailabilityOverrideResponse(
                "Override set successfully",
                override
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseBody);
    }

    // 🔎 Get Overrides (for provider)
    @GetMapping
    public ResponseEntity<?> getOverrides(
            @AuthenticationPrincipal User provider,
            @RequestParam(required = false) LocalDate date) {

        if((provider.getRole() != UserRoles.PROVIDER)){
            throw new AppException("AUTH-005", "Only Providers can only get Overrides");
        }

        Establishment establishment = establishmentRepository
                .findByOwner(provider)
                .orElseThrow(() -> new AppException("ESTAB-001", "Establishment not found"));

        if (date != null) {
            return ResponseEntity.ok(
                    overrideService.getOverride(establishment, date)
            );
        }

        return ResponseEntity.ok(
                overrideService.getAllOverrides(establishment)
        );
    }

    // ❌ Delete Override
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOverride(
            @PathVariable Long id,
            @AuthenticationPrincipal User provider){

        if((provider.getRole() != UserRoles.PROVIDER)){
            throw new AppException("AUTH-005", "Only Providers can delete Overrides");
        }

        Establishment establishment = establishmentRepository
                .findByOwner(provider)
                .orElseThrow(() -> new AppException("ESTAB-001", "Establishment not found"));

        overrideService.deleteOverride(id, establishment);

        return ResponseEntity.ok("Override deleted.");
    }
}