package edu.cit.ochavillo.schedease.controller;

import edu.cit.ochavillo.schedease.dto.AppointmentDTO;
import edu.cit.ochavillo.schedease.dto.AppointmentResponse;
import edu.cit.ochavillo.schedease.dto.BookAppointmentRequest;
import edu.cit.ochavillo.schedease.dto.RescheduleRequest;
import edu.cit.ochavillo.schedease.entity.Establishment;
import edu.cit.ochavillo.schedease.entity.User;
import edu.cit.ochavillo.schedease.enums.UserRoles;
import edu.cit.ochavillo.schedease.repository.EstablishmentRepository;
import edu.cit.ochavillo.schedease.repository.UserRepository;
import edu.cit.ochavillo.schedease.security.RateLimitPlan;
import edu.cit.ochavillo.schedease.service.AppointmentService;
import edu.cit.ochavillo.schedease.service.RateLimitingService;
import edu.cit.ochavillo.schedease.util.ApiErrorResponse;
import edu.cit.ochavillo.schedease.util.AppException;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
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
            @AuthenticationPrincipal String username,
            @RequestBody BookAppointmentRequest request) {

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

    // ✅ Confirm Appointment (Provider Only)
    @PutMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponse> confirmAppointment(
            @PathVariable UUID id,
            @AuthenticationPrincipal User provider) {

        if (provider.getRole() != UserRoles.PROVIDER) {
            throw new AppException("AUTH-005", "Only providers can confirm appointments.");
        }

        AppointmentDTO appointmentDTO = appointmentService.confirmAppointment(id, provider);

        AppointmentResponse responseBody = new AppointmentResponse(
                "Appointment Confirmed",
                appointmentDTO
        );

        return ResponseEntity.ok(responseBody);
    }

    // ✅ Cancel Appointment (Client or Provider)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable UUID id,
            @AuthenticationPrincipal User requester) {

        AppointmentDTO appointmentDTO = appointmentService.cancelAppointment(id, requester);

        AppointmentResponse responseBody = new  AppointmentResponse(
                "Appointment Cancelled.",
                appointmentDTO
        );

        return ResponseEntity.ok(responseBody);
    }

    // 📋 Get My Appointments (Client)
    @GetMapping("/my")
    public ResponseEntity<?> getMyAppointments(
            @AuthenticationPrincipal(expression = "username") String username) {

        User client = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("USER-001", "User not found"));

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

        if (!provider.getRole().equals("PROVIDER")) {
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
            @AuthenticationPrincipal String username,
            @RequestBody RescheduleRequest request,
            HttpServletRequest httpRequest) {

        String ip = httpRequest.getRemoteAddr();

        Bucket bucket = rateLimiter.resolveBucket(ip, RateLimitPlan.STANDARD);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ApiErrorResponse("429","Too many request attempts. Please wait 1 minute."));
        }

        User requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("USER-001", "User not found"));

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