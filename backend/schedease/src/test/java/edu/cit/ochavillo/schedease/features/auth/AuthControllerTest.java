package edu.cit.ochavillo.schedease.features.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.ochavillo.schedease.auth.controller.AuthController;
import edu.cit.ochavillo.schedease.auth.dto.LoginRequest;
import edu.cit.ochavillo.schedease.auth.dto.LoginResponse;
import edu.cit.ochavillo.schedease.auth.service.AuthService;
import edu.cit.ochavillo.schedease.security.JwtUtil;
import edu.cit.ochavillo.schedease.security.RateLimitingService;
import edu.cit.ochavillo.schedease.user.repository.UserRepository;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// 🚨 CHANGED: Using the new Spring 3.4.0 MockitoBean import
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 🚨 CHANGED: Replaced @MockBean with @MockitoBean
    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private RateLimitingService rateLimiter;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserRepository userRepository;

    private Bucket mockBucket;

    @BeforeEach
    public void setup() {
        mockBucket = Mockito.mock(Bucket.class);
        Mockito.when(rateLimiter.resolveBucket(anyString(), any())).thenReturn(mockBucket);
        Mockito.when(mockBucket.tryConsume(1)).thenReturn(true);
    }

    @Test
    public void login_ValidCredentials_Returns200AndSetsCookie() throws Exception {
        // Arrange
        // 🚨 CHANGED: Using empty constructor and setters instead of arguments
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser"); // Note: Change to request.setEmail() if your DTO uses email
        request.setPassword("password123");

        LoginResponse mockResponse = new LoginResponse("mock-access-token", "mock-refresh-token");

        Mockito.when(authService.login(any(LoginRequest.class), anyString(), anyString()))
                .thenReturn(mockResponse);

        // Act & Assert
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf()) // 🚨 ADD THIS LINE!
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "Mozilla/5.0")
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk())
                // ... rest of the assertions
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().value("refreshToken", "mock-refresh-token"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true));
    }

    @Test
    public void login_SpammingRequests_Returns429TooManyRequests() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("spammer");
        request.setPassword("password123");

        // Block the bucket for this test so RateLimiter triggers
        Mockito.when(mockBucket.tryConsume(1)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // 🚨 FIX 1: Expect 429 Too Many Requests (not 200 OK)
                .andExpect(status().isTooManyRequests())

                // 🚨 FIX 2: Update JSON path to match your ApiErrorResponse! ($.error.message)
                .andExpect(jsonPath("$.error.message").value("Too many login attempts. Please wait 1 minute."));

        // Ensure the authService was NEVER actually called
        Mockito.verify(authService, Mockito.never()).login(any(), any(), any());
    }
}