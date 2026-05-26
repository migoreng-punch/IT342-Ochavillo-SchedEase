package edu.cit.ochavillo.schedease.establishment.controller;

import edu.cit.ochavillo.schedease.establishment.dto.CreateEstablishmentRequest;
import edu.cit.ochavillo.schedease.establishment.dto.EstablishmentDTO;
import edu.cit.ochavillo.schedease.establishment.dto.EstablishmentResponse;
import edu.cit.ochavillo.schedease.establishment.dto.UpdateEstablishmentRequest;
import edu.cit.ochavillo.schedease.establishment.entity.Establishment;
import edu.cit.ochavillo.schedease.user.entity.User;
import edu.cit.ochavillo.schedease.user.enums.UserRoles;
import edu.cit.ochavillo.schedease.establishment.repository.EstablishmentRepository;
import edu.cit.ochavillo.schedease.user.repository.UserRepository;
import edu.cit.ochavillo.schedease.security.RateLimitPlan;
import edu.cit.ochavillo.schedease.availability.service.AvailabilityService;
import edu.cit.ochavillo.schedease.establishment.service.EstablishmentService;
import edu.cit.ochavillo.schedease.security.RateLimitingService;
import edu.cit.ochavillo.schedease.util.ApiErrorResponse;
import edu.cit.ochavillo.schedease.util.AppException;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/establishments")
public class EstablishmentController {

    private final AvailabilityService availabilityService;
    private final EstablishmentRepository establishmentRepository;
    private final EstablishmentService establishmentService;
    private final UserRepository userRepository;
    private final RateLimitingService rateLimiter;

    public EstablishmentController(AvailabilityService availabilityService,
                                   EstablishmentRepository establishmentRepository,
                                   EstablishmentService establishmentService,
                                   UserRepository userRepository,
                                   RateLimitingService rateLimiter) {

        this.availabilityService = availabilityService;
        this.establishmentRepository = establishmentRepository;
        this.establishmentService = establishmentService;
        this.userRepository = userRepository;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<?> getAvailableSlots(
            @PathVariable Long id,
            @RequestParam LocalDate date) {

        Establishment establishment = establishmentRepository.findById(id)
                .orElseThrow(() -> new AppException("ESTAB-001", "Establishment not found"));

        return ResponseEntity.ok(
                availabilityService.generateAvailableSlots(establishment, date)
        );
    }

    // 🔎 Browse & Search Establishments (Now with Cursor Pagination!)
    @GetMapping
    public ResponseEntity<?> getAllEstablishments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest httpRequest) { // Default to 10 items if mobile forgets to ask

        String ip = httpRequest.getRemoteAddr();

        Bucket bucket = rateLimiter.resolveBucket(ip, RateLimitPlan.SEARCH);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ApiErrorResponse("429","Too many Requests attempts. Please wait 1 minute."));
        }

        return ResponseEntity.ok(
                establishmentService.getEstablishments(search, cursor, limit)
        );
    }

    // 🔎 Get Single Establishment Details
    @GetMapping("/{id}")
    public ResponseEntity<?> getEstablishment(@PathVariable Long id, HttpServletRequest httpRequest) {

        String ip = httpRequest.getRemoteAddr();

        Bucket bucket = rateLimiter.resolveBucket(ip, RateLimitPlan.SEARCH);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ApiErrorResponse("429","Too many request. Please wait 1 minute."));
        }

        return ResponseEntity.ok(
                establishmentService.getEstablishmentById(id)
        );
    }

    // 🏢 Create Establishment (Provider)
    @PostMapping
    public ResponseEntity<EstablishmentResponse> createEstablishment(
            @AuthenticationPrincipal User provider,
            @RequestBody CreateEstablishmentRequest request) {

        if((provider.getRole() != UserRoles.PROVIDER)){
            throw new AppException("AUTH-005", "Only Providers can create Establishment");
        }

        EstablishmentDTO establishmentDto = establishmentService.createEstablishment(provider, request);

        EstablishmentResponse responseBody = new EstablishmentResponse(
                "Establishment created successfully!",
                establishmentDto
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseBody);
    }

    // ✏️ Update Establishment
    @PutMapping("/me")
    public ResponseEntity<EstablishmentResponse> updateMyEstablishment(
            @AuthenticationPrincipal User provider,
            @RequestBody UpdateEstablishmentRequest request) {

        if (provider.getRole() != UserRoles.PROVIDER) {
            throw new AppException("AUTH-005", "Only Providers can update Establishments");
        }

        Establishment establishment = establishmentRepository
                .findByOwner(provider)
                .orElseThrow(() -> new AppException("ESTAB-001", "Establishment not found"));

        // Reusing your existing service logic!
        EstablishmentDTO establishmentDto = establishmentService.updateEstablishment(
                establishment.getId(),
                provider,
                request
        );

        EstablishmentResponse responseBody = new EstablishmentResponse(
                "Establishment Updated Successfully",
                establishmentDto
        );

        return ResponseEntity.ok(responseBody);
    }

    // 🔎 Get MY Establishment Details
    @GetMapping("/me")
    public ResponseEntity<?> getMyEstablishment(@AuthenticationPrincipal User provider) {

        if (provider.getRole() != UserRoles.PROVIDER) {
            throw new AppException("AUTH-005", "Only Providers have an establishment");
        }

        Establishment establishment = establishmentRepository
                .findByOwner(provider)
                .orElseThrow(() -> new AppException("ESTAB-001", "Establishment not found"));

        // Reusing your existing service logic!
        return ResponseEntity.ok(
                establishmentService.getEstablishmentById(establishment.getId())
        );
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMyEstablishment(@AuthenticationPrincipal User provider) {

        // 1. Optional: You could add a check here to ensure the user is actually a PROVIDER
        // if (!provider.getRole().equals(UserRoles.PROVIDER)) {
        //     throw new AppException("AUTH-403", "Only providers can delete establishments.");
        // }

        // 2. Call the service
        establishmentService.deleteMyEstablishment(provider);

        // 3. Return a clean success message
        return ResponseEntity.ok(Map.of("message", "Establishment successfully deleted."));
    }
}
