package edu.cit.ochavillo.schedease.features.establishment;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.ochavillo.schedease.availability.service.AvailabilityService;
import edu.cit.ochavillo.schedease.establishment.controller.EstablishmentController;
import edu.cit.ochavillo.schedease.establishment.dto.CreateEstablishmentRequest;
import edu.cit.ochavillo.schedease.establishment.dto.EstablishmentDTO;
import edu.cit.ochavillo.schedease.establishment.entity.Establishment;
import edu.cit.ochavillo.schedease.establishment.repository.EstablishmentRepository;
import edu.cit.ochavillo.schedease.establishment.service.EstablishmentService;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EstablishmentController.class)
public class EstablishmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 🚨 Mock ALL dependencies found in the controller constructor
    @MockitoBean
    private AvailabilityService availabilityService;

    @MockitoBean
    private EstablishmentRepository establishmentRepository;

    @MockitoBean
    private EstablishmentService establishmentService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RateLimitingService rateLimiter;

    // 🚨 Satisfy the JwtAuthenticationFilter
    @MockitoBean
    private JwtUtil jwtUtil;

    private Bucket mockBucket;
    private User mockProvider;
    private User mockClient;

    @BeforeEach
    public void setup() {
        // Setup Rate Limiter
        mockBucket = Mockito.mock(Bucket.class);
        Mockito.when(rateLimiter.resolveBucket(anyString(), any())).thenReturn(mockBucket);
        Mockito.when(mockBucket.tryConsume(1)).thenReturn(true);

        // Setup Mock Provider User
        mockProvider = new User();
        mockProvider.setUsername("shop_owner");
        mockProvider.setRole(UserRoles.PROVIDER);

        // Setup Mock Client User
        mockClient = new User();
        mockClient.setUsername("regular_client");
        mockClient.setRole(UserRoles.CLIENT);
    }

    @Test
    public void getAvailableSlots_ValidIdAndDate_ReturnsSlots() throws Exception {
        // Arrange
        Long establishmentId = 1L;
        LocalDate testDate = LocalDate.parse("2026-05-15");
        Establishment mockEstablishment = new Establishment();

        // Mock DB lookup
        Mockito.when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(mockEstablishment));

        // Mock Availability Service
        List<LocalTime> mockSlots = List.of(
                LocalTime.parse("09:00:00"),
                LocalTime.parse("10:00:00")
        );
        Mockito.when(availabilityService.generateAvailableSlots(any(Establishment.class), eq(testDate)))
                .thenReturn(mockSlots); // Assuming it returns a List or similar object

        // Act & Assert
        mockMvc.perform(get("/api/establishments/{id}/slots", establishmentId)
                        .param("date", "2026-05-15")
                        // Using an unauthenticated user (or general user) for a public-ish endpoint
                        .with(authentication(new UsernamePasswordAuthenticationToken(mockClient, null, List.of()))))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("09:00:00"))
                .andExpect(jsonPath("$[1]").value("10:00:00"));
    }

    @Test
    public void getAllEstablishments_SpammingRequests_Returns429TooManyRequests() throws Exception {
        // Arrange
        // Force the Bucket4j rate limiter to deny the request
        Mockito.when(mockBucket.tryConsume(1)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/establishments")
                        .param("search", "Coffee")
                        .with(authentication(new UsernamePasswordAuthenticationToken(mockClient, null, List.of()))))

                .andExpect(status().isTooManyRequests())
                // Assert nested error message based on your ApiErrorResponse class
                .andExpect(jsonPath("$.error.message").value("Too many Requests attempts. Please wait 1 minute."));

        // Verify the service was NEVER called
        Mockito.verify(establishmentService, Mockito.never()).getEstablishments(anyString(), any(), anyInt());
    }

    @Test
    public void createEstablishment_AsProvider_Returns201Created() throws Exception {
        // Arrange
        // We use a raw JSON string to bypass needing to know the exact fields of CreateEstablishmentRequest
        String requestJson = "{\"name\":\"New Barbershop\", \"address\":\"123 Main St\"}";

        EstablishmentDTO mockDto = Mockito.mock(EstablishmentDTO.class);

        Mockito.when(establishmentService.createEstablishment(any(User.class), any(CreateEstablishmentRequest.class)))
                .thenReturn(mockDto);

        // Act & Assert
        mockMvc.perform(post("/api/establishments")
                        // 🚨 Must inject the Provider to pass the UserRoles.PROVIDER check!
                        .with(authentication(new UsernamePasswordAuthenticationToken(mockProvider, null, List.of())))
                        .with(csrf()) // Handle POST security
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Establishment created successfully!"));
    }
}