package edu.cit.ochavillo.schedease.features.auth;

import edu.cit.ochavillo.schedease.auth.dto.RegisterRequest;
import edu.cit.ochavillo.schedease.auth.service.AuthService;
import edu.cit.ochavillo.schedease.user.entity.User;
import edu.cit.ochavillo.schedease.user.repository.UserRepository;
import edu.cit.ochavillo.schedease.user.enums.UserRoles;
// Add your imports for these new mocks!
import edu.cit.ochavillo.schedease.auth.repository.VerificationTokenRepository;
// import edu.cit.ochavillo.schedease.email.EmailService;

import edu.cit.ochavillo.schedease.util.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    // 1. ADDED MOCK: The repository for the new Email Confirmation feature
    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    // NOTE: If your AuthService also sends an email directly, uncomment this!
     @Mock
     private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerUser_ThrowsException_IfEmailExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("test@email.com");
        request.setPassword("password123");

        // 2. FIX: We mock BOTH ways your service might be checking for duplicates
        // to ensure it triggers your custom exception properly.
        when(userRepository.existsByEmail("test@email.com")).thenReturn(true);
        // If your service uses findByEmail instead of existsByEmail, uncomment below:
        // when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authService.register(request);
        }, "Should throw an exception if the email is already taken.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_SavesUser_WithEncodedPassword() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail("new@email.com");
        request.setPassword("password123");

        // Mock database responses
        when(userRepository.existsByEmail("new@email.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // Mock the user save to return a valid user object (needed for the token creation)
        User savedUser = new User();
        savedUser.setEmail("new@email.com");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        authService.register(request);

        // Assert
        verify(userRepository, times(1)).save(any(User.class));

        // 3. FIX: Also verify that the token was saved!
        verify(verificationTokenRepository, times(1)).save(any());
    }
}