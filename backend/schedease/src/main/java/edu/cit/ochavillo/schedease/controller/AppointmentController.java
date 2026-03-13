package edu.cit.ochavillo.schedease.controller;

import edu.cit.ochavillo.schedease.dto.BookAppointmentRequest;
import edu.cit.ochavillo.schedease.entity.Establishment;
import edu.cit.ochavillo.schedease.entity.User;
import edu.cit.ochavillo.schedease.repository.EstablishmentRepository;
import edu.cit.ochavillo.schedease.repository.UserRepository;
import edu.cit.ochavillo.schedease.service.AppointmentService;
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

    public AppointmentController(AppointmentService appointmentService,
                                 UserRepository userRepository, EstablishmentRepository establishmentRepository) {
        this.appointmentService = appointmentService;
        this.userRepository = userRepository;
        this.establishmentRepository = establishmentRepository;
    }

    // ✅ Book Appointment (Client)
    @PostMapping
    public ResponseEntity<?> bookAppointment(
            @AuthenticationPrincipal String username,
            @RequestBody BookAppointmentRequest request) {

        User client = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Establishment establishment = establishmentRepository.findById(request.establishmentId())
                .orElseThrow(() -> new RuntimeException("Establishment not found"));

        appointmentService.bookAppointment(
                client,
                establishment,
                request.date(),
                request.startTime()
        );

        return ResponseEntity.ok("Appointment booked. Awaiting confirmation.");
    }

    // ✅ Confirm Appointment (Provider Only)
    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirmAppointment(
            @PathVariable UUID id,
            @AuthenticationPrincipal String username) {

        User provider = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        appointmentService.confirmAppointment(id, provider);

        return ResponseEntity.ok("Appointment confirmed.");
    }

    // ✅ Cancel Appointment (Client or Provider)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable UUID id,
            @AuthenticationPrincipal String username) {

        User requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        appointmentService.cancelAppointment(id, requester);

        return ResponseEntity.ok("Appointment cancelled.");
    }

    // 📋 Get My Appointments (Client)
    @GetMapping("/my")
    public ResponseEntity<?> getMyAppointments(
            @AuthenticationPrincipal(expression = "username") String username) {

        User client = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                appointmentService.getAppointmentsForClient(client)
        );
    }

    // 📋 Get Provider Appointments
    @GetMapping("/provider")
    public ResponseEntity<?> getProviderAppointments(
            @AuthenticationPrincipal(expression = "username") String username) {

        User provider = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!provider.getRole().equals("PROVIDER")) {
            throw new RuntimeException("Only providers can access this endpoint.");
        }

        Establishment establishment = establishmentRepository
                .findByOwner(provider)
                .orElseThrow(() -> new RuntimeException("Establishment not found"));

        return ResponseEntity.ok(
                appointmentService.getAppointmentsForEstablishment(establishment)
        );
    }
}