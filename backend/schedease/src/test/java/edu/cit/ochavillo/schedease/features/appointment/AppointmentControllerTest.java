package edu.cit.ochavillo.schedease.features.appointment;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.ochavillo.schedease.appointment.controller.AppointmentController;
import edu.cit.ochavillo.schedease.appointment.dto.AppointmentDTO;
import edu.cit.ochavillo.schedease.appointment.dto.BookAppointmentRequest;
import edu.cit.ochavillo.schedease.appointment.dto.RescheduleRequest;
import edu.cit.ochavillo.schedease.appointment.service.AppointmentService;
import edu.cit.ochavillo.schedease.establishment.entity.Establishment;
import edu.cit.ochavillo.schedease.establishment.repository.EstablishmentRepository;
import edu.cit.ochavillo.schedease.security.JwtUtil;
import edu.cit.ochavillo.schedease.security.RateLimitingService;
import edu.cit.ochavillo.schedease.user.entity.User;
import edu.cit.ochavillo.schedease.user.enums.UserRoles;
import edu.cit.ochavillo.schedease.user.repository.UserRepository;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// 🚨 Added these imports for parsing dates and times
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentController.class)
public class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private EstablishmentRepository establishmentRepository;

    @MockitoBean
    private RateLimitingService rateLimiter;

    @MockitoBean
    private JwtUtil jwtUtil;

    private Bucket mockBucket;
    private User mockClient;
    private User mockProvider;

    @BeforeEach
    public void setup() {
        mockBucket = Mockito.mock(Bucket.class);
        Mockito.when(rateLimiter.resolveBucket(anyString(), any())).thenReturn(mockBucket);
        Mockito.when(mockBucket.tryConsume(1)).thenReturn(true);

        mockClient = new User();
        mockClient.setUsername("clientuser");
        mockClient.setRole(UserRoles.USER);

        mockProvider = new User();
        mockProvider.setUsername("provideruser");
        mockProvider.setRole(UserRoles.PROVIDER);
    }

    @Test
    public void bookAppointment_ValidData_Returns201Created() throws Exception {
        // Arrange
        Long establishmentId = 1L;

        // 🚨 FIX 1: Use LocalDate.parse and LocalTime.parse to satisfy the record's types
        BookAppointmentRequest request = new BookAppointmentRequest(
                establishmentId,
                LocalDate.parse("2026-05-15"),
                LocalTime.parse("10:00:00")
        );

        Establishment mockEstablishment = new Establishment();

        // 🚨 FIX 2: Mock the DTO so we don't have to fill out all 7 arguments
        AppointmentDTO mockDto = Mockito.mock(AppointmentDTO.class);

        Mockito.when(userRepository.findByUsername("clientuser")).thenReturn(Optional.of(mockClient));
        Mockito.when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(mockEstablishment));

        // 🚨 FIX 3: Update matchers to expect LocalDate and LocalTime classes instead of Strings
        Mockito.when(appointmentService.bookAppointment(
                any(), any(), any(LocalDate.class), any(LocalTime.class)
        )).thenReturn(mockDto);

        // Act & Assert
        mockMvc.perform(post("/api/appointments")
                        .with(authentication(new UsernamePasswordAuthenticationToken(mockClient, null, List.of())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Appointment Booked Successfully."));
    }

    @Test
    public void confirmAppointment_AsProvider_Returns200OK() throws Exception {
        // Arrange
        UUID appointmentId = UUID.randomUUID();

        // 🚨 FIX 2 repeated: Mock the DTO
        AppointmentDTO mockDto = Mockito.mock(AppointmentDTO.class);

        Mockito.when(appointmentService.confirmAppointment(any(UUID.class), any(User.class)))
                .thenReturn(mockDto);

        // Act & Assert
        mockMvc.perform(put("/api/appointments/{id}/confirm", appointmentId)
                        .with(authentication(new UsernamePasswordAuthenticationToken(mockProvider, null, List.of())))
                        .with(csrf()))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Appointment Confirmed"));
    }

    @Test
    public void rescheduleAppointment_SpammingRequests_Returns429TooManyRequests() throws Exception {
        // Arrange
        UUID appointmentId = UUID.randomUUID();

        // 🚨 FIX 1 repeated: Use Parsers for the Reschedule record
        RescheduleRequest request = new RescheduleRequest(
                LocalDate.parse("2026-05-20"),
                LocalTime.parse("14:00:00")
        );

        Mockito.when(mockBucket.tryConsume(1)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(put("/api/appointments/{id}/reschedule", appointmentId)
                        .with(authentication(new UsernamePasswordAuthenticationToken("clientuser", null, List.of())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error.message").value("Too many request attempts. Please wait 1 minute."));

        Mockito.verify(userRepository, Mockito.never()).findByUsername(anyString());
    }
}