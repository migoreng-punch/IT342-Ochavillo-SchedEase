package edu.cit.ochavillo.schedease.features.establishment;

import edu.cit.ochavillo.schedease.establishment.dto.CreateEstablishmentRequest;
import edu.cit.ochavillo.schedease.establishment.entity.Establishment;
import edu.cit.ochavillo.schedease.establishment.repository.EstablishmentRepository;
import edu.cit.ochavillo.schedease.establishment.service.EstablishmentService;
import edu.cit.ochavillo.schedease.user.entity.User;
import edu.cit.ochavillo.schedease.user.enums.UserRoles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstablishmentServiceTest {

    @Mock
    private EstablishmentRepository establishmentRepository;

    @InjectMocks
    private EstablishmentService establishmentService;

    @Test
    void createEstablishment_Fails_IfUserIsClient() {
        // Arrange: Create a standard user (Client)
        User clientUser = new User();
        clientUser.setRole(UserRoles.CLIENT); // Using the enum from your auth tests

        // Arrange: Instantiate the record using the auto-generated constructor
        CreateEstablishmentRequest request = new CreateEstablishmentRequest(
                "Barber Shop",          // name
                "Fresh cuts daily",     // description
                "123 Main St",          // address
                "barber@email.com",     // contactEmail
                30                      // slotDurationMinutes
        );

        // Act & Assert
        Exception exception = assertThrows(SecurityException.class, () -> {
            establishmentService.createEstablishment(clientUser, request);
        });

        // Verify the exception message matches your actual service logic
        assertEquals("Only providers can create establishments.", exception.getMessage());

        // Verify the database never saved anything
        verify(establishmentRepository, never()).save(any());
    }

    @Test
    void createEstablishment_Saves_IfUserIsProvider() {
        // Arrange: Create a Provider user
        User providerUser = new User();
        providerUser.setRole(UserRoles.PROVIDER); // Assuming you have this role

        CreateEstablishmentRequest request = new CreateEstablishmentRequest(
                "Salon Elegance",
                "Premium hair styling",
                "456 Oak Ave",
                "contact@salon.com",
                60
        );

        // Act
        establishmentService.createEstablishment(providerUser, request);

        // Assert: Verify the repository's save method was called exactly once
        verify(establishmentRepository, times(1)).save(any(Establishment.class));
    }
}