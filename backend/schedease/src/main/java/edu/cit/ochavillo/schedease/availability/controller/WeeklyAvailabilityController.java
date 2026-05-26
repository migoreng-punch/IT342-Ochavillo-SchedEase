package edu.cit.ochavillo.schedease.availability.controller;

import edu.cit.ochavillo.schedease.availability.dto.AvailabilityResponse;
import edu.cit.ochavillo.schedease.availability.dto.CreateWeeklyAvailabilityRequest;
import edu.cit.ochavillo.schedease.availability.entity.WeeklyAvailability;
import edu.cit.ochavillo.schedease.establishment.entity.Establishment;
import edu.cit.ochavillo.schedease.user.entity.User;
import edu.cit.ochavillo.schedease.establishment.repository.EstablishmentRepository;
import edu.cit.ochavillo.schedease.user.enums.UserRoles;
import edu.cit.ochavillo.schedease.user.repository.UserRepository;
import edu.cit.ochavillo.schedease.availability.service.AvailabilityService;
import edu.cit.ochavillo.schedease.util.AppException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/providers/availability")
public class WeeklyAvailabilityController {

    private final AvailabilityService availabilityService;
    private final UserRepository userRepository;
    private final EstablishmentRepository establishmentRepository;

    public WeeklyAvailabilityController(
            AvailabilityService availabilityService,
            UserRepository userRepository,
            EstablishmentRepository establishmentRepository) {
        this.availabilityService = availabilityService;
        this.userRepository = userRepository;
        this.establishmentRepository = establishmentRepository;
    }

    @PostMapping
    public ResponseEntity<?> createAvailability(
            @AuthenticationPrincipal(expression = "username") String username,
            @RequestBody List<CreateWeeklyAvailabilityRequest> requests) { // 🚨 Accepts the List

        User provider = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("USER-001", "User not found"));

        Establishment establishment = establishmentRepository
                .findByOwner(provider)
                .orElseThrow(() -> new AppException("ESTAB-001", "Establishment not found"));

        // Ensure we check the Enum properly!
        if(provider.getRole() != UserRoles.PROVIDER) {
            throw new AppException("AUTH-005", "Only Providers can create availability");
        }

        // 🚨 Loop through the array of 7 days sent by React
        for (CreateWeeklyAvailabilityRequest request : requests) {
            // Only save days that actually have times (skip the "off" days)
            if (request.startTime() != null && request.endTime() != null) {
                availabilityService.createWeeklyAvailability(
                        provider,
                        establishment,
                        request.dayOfWeek(),
                        request.startTime(),
                        request.endTime()
                );
            }
        }

        return ResponseEntity.ok("Weekly availability created.");
    }

    @PutMapping
    public ResponseEntity<?> updateAvailability(
            @AuthenticationPrincipal(expression = "username") String username,
            @RequestBody List<CreateWeeklyAvailabilityRequest> requests) {

        User provider = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("USER-001", "User not found"));

        Establishment establishment = establishmentRepository
                .findByOwner(provider)
                .orElseThrow(() -> new AppException("ESTAB-001", "Establishment not found"));

        if(provider.getRole() != UserRoles.PROVIDER) {
            throw new AppException("AUTH-005", "Only Providers can update availability");
        }

        // 🚨 1. WIPE: Delete the old schedule completely
        // You will need to add this method to your AvailabilityService/Repository!
        availabilityService.deleteAllByProviderAndEstablishment(establishment);

        // 🚨 2. REPLACE: Insert the new schedule
        for (CreateWeeklyAvailabilityRequest request : requests) {
            // Only save days that actually have times
            if (request.startTime() != null && request.endTime() != null) {
                availabilityService.createWeeklyAvailability(
                        provider,
                        establishment,
                        request.dayOfWeek(),
                        request.startTime(),
                        request.endTime()
                );
            }
        }

        return ResponseEntity.ok("Weekly availability updated successfully.");
    }


    @GetMapping
    public ResponseEntity<?> getAvailability(
            @AuthenticationPrincipal(expression = "username") String username) {

        System.out.println("\n--- FETCHING SCHEDULE FOR USER: " + username + " ---\n");

        // 1. Authenticate and identify the user
        User provider = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("USER-001", "User not found"));

        // 2. Find their establishment
        Establishment establishment = establishmentRepository
                .findByOwner(provider)
                .orElseThrow(() -> new AppException("ESTAB-001", "Establishment not found"));

        // 3. Fetch the raw database entities
        List<WeeklyAvailability> rawAvailability = availabilityService.getWeeklyAvailability(establishment);

        // 4. Map entities to clean DTOs for React
        List<AvailabilityResponse> response = rawAvailability.stream()
                .map(avail -> new AvailabilityResponse(
                        avail.getDayOfWeek().name(), // Converts Enum to String (e.g., "MONDAY")
                        avail.getStartTime(),
                        avail.getEndTime()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }
}