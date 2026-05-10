package edu.cit.ochavillo.schedease.features.appointment;

import edu.cit.ochavillo.schedease.appointment.entity.Appointment;
import edu.cit.ochavillo.schedease.appointment.repository.AppointmentRepository;
import edu.cit.ochavillo.schedease.appointment.service.AppointmentService;
import edu.cit.ochavillo.schedease.availability.service.AvailabilityService;
import edu.cit.ochavillo.schedease.establishment.entity.Establishment;
import edu.cit.ochavillo.schedease.user.entity.User;
import edu.cit.ochavillo.schedease.util.AppException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AvailabilityService availabilityService;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void bookAppointment_Fails_IfSlotIsNoLongerAvailable() {
        // Arrange
        User clientUser = new User();
        Establishment mockEst = new Establishment();
        mockEst.setId(1L);
        mockEst.setSlotDurationMinutes(30);

        LocalDate date = LocalDate.of(2026, 5, 12);
        LocalTime requestedTime = LocalTime.of(14, 0); // 2:00 PM

        // Mock availability: Let's pretend ONLY 1:00 PM is available (meaning 2:00 PM is taken)
        when(availabilityService.generateAvailableSlots(mockEst, date))
                .thenReturn(List.of(LocalTime.of(13, 0)));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            appointmentService.bookAppointment(clientUser, mockEst, date, requestedTime);
        }, "Should throw an AppException if the requested time was already taken.");

        // Optional but highly recommended: verify the exact error message!
        assertEquals("Selected slot is not available.", exception.getMessage());

        // Ensure nothing was saved to the database
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void bookAppointment_Saves_IfSlotIsAvailable() {
        // Arrange
        User clientUser = new User();
        Establishment mockEst = new Establishment();
        mockEst.setId(1L);
        mockEst.setSlotDurationMinutes(30);

        LocalDate date = LocalDate.of(2026, 5, 12);
        LocalTime requestedTime = LocalTime.of(14, 0); // 2:00 PM

        // Mock availability: The requested 2:00 PM slot IS available
        when(availabilityService.generateAvailableSlots(mockEst, date))
                .thenReturn(List.of(LocalTime.of(14, 0)));

        // Act
        appointmentService.bookAppointment(clientUser, mockEst, date, requestedTime);

        // Assert: Ensure it was saved exactly once!
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }
}