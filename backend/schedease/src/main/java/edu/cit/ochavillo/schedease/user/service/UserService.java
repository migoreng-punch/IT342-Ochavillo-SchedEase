package edu.cit.ochavillo.schedease.user.service;

import edu.cit.ochavillo.schedease.auth.repository.VerificationTokenRepository;
import edu.cit.ochavillo.schedease.auth.service.RefreshTokenService;
import edu.cit.ochavillo.schedease.user.dto.ChangePasswordRequest;
import edu.cit.ochavillo.schedease.util.AppException;
import edu.cit.ochavillo.schedease.user.dto.UpdateUserRequest;
import edu.cit.ochavillo.schedease.user.dto.UserResponse;
import edu.cit.ochavillo.schedease.user.entity.User;
import edu.cit.ochavillo.schedease.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       RefreshTokenService refreshTokenService,
                       VerificationTokenRepository verificationTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- GET USER PROFILE ---
    public UserResponse getUserProfile(User authenticatedUser) {
        return new UserResponse(
                authenticatedUser.getUsername(),
                authenticatedUser.getFirstName(),
                authenticatedUser.getLastName(),
                authenticatedUser.getPhoneNumber(),
                authenticatedUser.getAddress(),
                authenticatedUser.isEmailVerified(),
                authenticatedUser.getEmail(),
                authenticatedUser.getRole().name(),
                authenticatedUser.getCreatedAt()
        );
    }

    // --- UPDATE USER PROFILE ---
    @Transactional
    public UserResponse updateUserProfile(User authenticatedUser, UpdateUserRequest request) {
        // 1. Check if the new username is taken by someone else
        if (!authenticatedUser.getUsername().equals(request.username()) &&
                userRepository.existsByUsername(request.username())) {
            throw new AppException("USER-002", "Username is already taken");
        }

        // 2. Check if the new email is taken by someone else
//        if (!authenticatedUser.getEmail().equals(request.email()) &&
//                userRepository.existsByEmail(request.email())) {
//            throw new AppException("USER-003", "Email is already in use");
//        }

        // 3. Apply updates
        authenticatedUser.setUsername(request.username());
        authenticatedUser.setFirstName(request.firstName());
        authenticatedUser.setLastName(request.lastName());
        authenticatedUser.setEmail(request.email());
        authenticatedUser.setAddress(request.address());
        authenticatedUser.setPhoneNumber(request.phoneNumber());

        // 4. Save to database
        User updatedUser = userRepository.save(authenticatedUser);

        return new UserResponse(
                updatedUser.getUsername(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getPhoneNumber(),
                updatedUser.getAddress(),
                updatedUser.isEmailVerified(),
                updatedUser.getEmail(),
                updatedUser.getRole().name(),
                updatedUser.getCreatedAt()
        );
    }

    public void changePassword(User user, ChangePasswordRequest request) {

        // 1. Verify that the new passwords match exactly
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new AppException("USER-001", "New passwords do not match.");
        }

        // 2. Verify the current password is correct
        // Note: passwordEncoder.matches(rawPassword, encodedPassword)
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new AppException("USER-002", "Current password is incorrect.");
        }

        // 3. (Optional but recommended) Prevent reusing the exact same password
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new AppException("USER-003", "New password cannot be the same as your current password.");
        }

        // 4. Encode and save the new password
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    // --- DELETE USER ACCOUNT ---
    @Transactional
    public void deleteUserAccount(User authenticatedUser) {
        // Because of Spring Data JPA, deleting the user should cascade and delete
        // their establishment, availability, and appointments IF your entity relationships
        // are set up with CascadeType.ALL or CascadeType.REMOVE.
        userRepository.delete(authenticatedUser);
    }
}