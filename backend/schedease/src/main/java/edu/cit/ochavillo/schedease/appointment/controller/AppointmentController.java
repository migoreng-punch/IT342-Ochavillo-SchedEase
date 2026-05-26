package edu.cit.ochavillo.schedease.appointment.controller;

import edu.cit.ochavillo.schedease.appointment.dto.*;
import edu.cit.ochavillo.schedease.establishment.entity.Establishment;
import edu.cit.ochavillo.schedease.user.entity.User;
import edu.cit.ochavillo.schedease.user.enums.UserRoles;
import edu.cit.ochavillo.schedease.establishment.repository.EstablishmentRepository;
import edu.cit.ochavillo.schedease.user.repository.UserRepository;
import edu.cit.ochavillo.schedease.security.RateLimitPlan;
import edu.cit.ochavillo.schedease.appointment.service.AppointmentService;
import edu.cit.ochavillo.schedease.security.RateLimitingService;
import edu.cit.ochavillo.schedease.util.ApiErrorResponse;
import edu.cit.ochavillo.schedease.util.AppException;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserRepository userRepository;
    private final EstablishmentRepository establishmentRepository;
    private final RateLimitingService rateLimiter;

    public AppointmentController(AppointmentService appointmentService,
                                 UserRepository userRepository,
                                 EstablishmentRepository establishmentRepository,
                                 RateLimitingService rateLimiter) {
        this.appointmentService = appointmentService;
        this.userRepository = userRepository;
        this.establishmentRepository = establishmentRepository;
        this.rateLimiter = rateLimiter;
    }

    // ✅ Book Appointment (Client)
    @PostMapping
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @AuthenticationPrincipal User user,
            @RequestBody BookAppointmentRequest request) {

        String username = user.getUsername();

        User client = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("USER-001", "User not found"));

        Establishment establishment = establishmentRepository.findById(request.establishmentId())
                .orElseThrow(() -> new AppException("ESTAB-001", "Establishment not found"));

        AppointmentDTO appointmentDTO = appointmentService.bookAppointment(
                client,
                establishment,
                request.date(),
                request.startTime()
        );

        AppointmentResponse responseBody = new AppointmentResponse(
                "Appointment Booked Successfully.",
                appointmentDTO
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(responseBody);
    }

    // 🔄 Update Appointment Status
    @PutMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal User user) {

        AppointmentDTO appointmentDTO = appointmentService.updateStatus(id, request.status(), user);

        return ResponseEntity.ok(new AppointmentResponse(
                "Appointment status updated to " + request.status().toUpperCase(),
                appointmentDTO
        ));
    }

    // 📋 Get My Appointments (Client)
    @GetMapping("/my")
    public ResponseEntity<?> getMyAppointments(
            @AuthenticationPrincipal User client) {

        return ResponseEntity.ok(
                appointmentService.getAppointmentsForClient(client)
        );
    }

    // 📋 Get Provider Appointments
    @GetMapping("/provider")
    public ResponseEntity<?> getProviderAppointments(
            @AuthenticationPrincipal(expression = "username") String username) {

        User provider = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("USER-001", "User not found"));

        if (provider.getRole() != UserRoles.PROVIDER) {
            throw new AppException("AUTH-005", "Only providers can access this endpoint.");
        }

        Establishment establishment = establishmentRepository
                .findByOwner(provider)
                .orElseThrow(() -> new AppException("ESTAB-001", "Establishment not found"));

        return ResponseEntity.ok(
                appointmentService.getAppointmentsForEstablishment(establishment)
        );
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<?> rescheduleAppointment(
            @PathVariable UUID id,
            @AuthenticationPrincipal User requester,
            @RequestBody RescheduleRequest request,
            HttpServletRequest httpRequest) {

        String ip = httpRequest.getRemoteAddr();

        Bucket bucket = rateLimiter.resolveBucket(ip, RateLimitPlan.STANDARD);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ApiErrorResponse("429","Too many request attempts. Please wait 1 minute."));
        }

        AppointmentDTO appointmentDTO = appointmentService.rescheduleAppointment(
                id,
                requester,
                request.date(),
                request.startTime()
        );

        AppointmentResponse responseBody = new AppointmentResponse(
                "Appointment rescheduled. Awaiting confirmation.",
                appointmentDTO
        );

        return ResponseEntity.ok(responseBody);
    }
}