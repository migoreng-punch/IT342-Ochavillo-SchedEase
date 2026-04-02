package edu.cit.ochavillo.schedease.controller;

import edu.cit.ochavillo.schedease.dto.*;
import edu.cit.ochavillo.schedease.entity.Establishment;
import edu.cit.ochavillo.schedease.entity.User;
import edu.cit.ochavillo.schedease.enums.UserRoles;
import edu.cit.ochavillo.schedease.repository.EstablishmentRepository;
import edu.cit.ochavillo.schedease.repository.UserRepository;
import edu.cit.ochavillo.schedease.service.AvailabilityService;
import edu.cit.ochavillo.schedease.service.EstablishmentService;
import edu.cit.ochavillo.schedease.util.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
                .orElseThrow(() -> new AppException("ESTAB-001", "Establishment not found"));

        return ResponseEntity.ok(
                availabilityService.generateAvailableSlots(establishment, date)
        );
    }

    // 🔎 Browse & Search Establishments (Now with Cursor Pagination!)
    @GetMapping
    public ResponseEntity<CursorResponse<EstablishmentDTO>> getAllEstablishments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int limit) { // Default to 10 items if mobile forgets to ask

        return ResponseEntity.ok(
                establishmentService.getEstablishments(search, cursor, limit)
        );
    }

    // 🔎 Get Single Establishment Details
    @GetMapping("/{id}")
    public ResponseEntity<EstablishmentDTO> getEstablishment(@PathVariable Long id) {

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
    @PutMapping("/{id}")
    public ResponseEntity<EstablishmentResponse> updateEstablishment(
            @PathVariable Long id,
            @AuthenticationPrincipal User provider,
            @RequestBody UpdateEstablishmentRequest request) {

        if((provider.getRole() != UserRoles.PROVIDER)){
            throw new AppException("AUTH-005", "Only Providers can create Establishment");
        }

        EstablishmentDTO establishmentDto = establishmentService.updateEstablishment(id, provider, request);

        EstablishmentResponse responseBody  = new EstablishmentResponse(
                "Establishment Updated Successfully",
                establishmentDto
        );

        return ResponseEntity.ok(responseBody);
    }
}
