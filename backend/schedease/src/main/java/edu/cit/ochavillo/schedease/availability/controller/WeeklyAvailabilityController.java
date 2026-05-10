package edu.cit.ochavillo.schedease.availability.controller;

import edu.cit.ochavillo.schedease.availability.dto.CreateWeeklyAvailabilityRequest;
import edu.cit.ochavillo.schedease.establishment.entity.Establishment;
import edu.cit.ochavillo.schedease.user.entity.User;
import edu.cit.ochavillo.schedease.establishment.repository.EstablishmentRepository;
import edu.cit.ochavillo.schedease.user.enums.UserRoles;
import edu.cit.ochavillo.schedease.user.repository.UserRepository;
import edu.cit.ochavillo.schedease.availability.service.AvailabilityService;
import edu.cit.ochavillo.schedease.util.AppException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestBody CreateWeeklyAvailabilityRequest request) {

        User provider = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("USER-001", "User not found"));

        Establishment establishment = establishmentRepository
                .findByOwner(provider)
                .orElseThrow(() -> new AppException("ESTAB-001", "Establishment not found"));

        if((provider.getRole() != UserRoles.PROVIDER)){
            throw new AppException("AUTH-005", "Only Providers can create Establishment");
        }

        availabilityService.createWeeklyAvailability(
                provider,
                establishment,
                request.dayOfWeek(),
                request.startTime(),
                request.endTime()
        );

        return ResponseEntity.ok("Weekly availability created.");
    }
}