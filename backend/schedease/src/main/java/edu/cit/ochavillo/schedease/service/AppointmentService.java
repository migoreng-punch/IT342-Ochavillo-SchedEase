package edu.cit.ochavillo.schedease.service;

import edu.cit.ochavillo.schedease.entity.Appointment;
import edu.cit.ochavillo.schedease.entity.Establishment;
import edu.cit.ochavillo.schedease.entity.User;
import edu.cit.ochavillo.schedease.entity.WeeklyAvailability;
import edu.cit.ochavillo.schedease.enums.AppointmentStatus;
import edu.cit.ochavillo.schedease.repository.AppointmentRepository;
import edu.cit.ochavillo.schedease.repository.WeeklyAvailabilityRepository;
import edu.cit.ochavillo.schedease.util.AppException;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final WeeklyAvailabilityRepository availabilityRepository;
    private final AvailabilityService availabilityService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            WeeklyAvailabilityRepository availabilityRepository,
            AvailabilityService availabilityService) {

        this.appointmentRepository = appointmentRepository;
        this.availabilityRepository = availabilityRepository;
        this.availabilityService = availabilityService;
    }

    @Transactional
    public void bookAppointment(User client,
                                Establishment establishment,
                                LocalDate date,
                                LocalTime start) {

        List<LocalTime> validSlots = availabilityService.generateAvailableSlots(establishment, date);

        int duration = establishment.getSlotDurationMinutes();
        LocalTime end = start.plusMinutes(duration);

        if (!validSlots.contains(start)) {
            throw new RuntimeException("Selected slot is not available.");
        }

        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        if (date.isBefore(today)) {
            throw new AppException("APPT-006", "Cannot book an appointment in the past.");
        }

        if (date.isEqual(today) && start.isBefore(nowTime)) {
            throw new AppException("APPT-006", "Cannot book a past time slot.");
        }

        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setEstablishment(establishment);
        appointment.setAppointmentDate(date);
        appointment.setStartTime(start);
        appointment.setEndTime(end);
        appointment.setStatus(AppointmentStatus.PENDING);

        try {
            appointmentRepository.save(appointment);
        } catch (DataIntegrityViolationException e) {
            throw new AppException("APPT-009", "This time slot has already been booked.");
        }
    }

    private void validateWeeklyAvailability(Establishment establishment,
                                            LocalDate date,
                                            LocalTime start,
                                            LocalTime end) {

        DayOfWeek day = date.getDayOfWeek();

        List<WeeklyAvailability> schedules =
                availabilityRepository.findByEstablishmentAndDayOfWeek(establishment, day);

        boolean valid = schedules.stream().anyMatch(schedule ->
                !start.isBefore(schedule.getStartTime()) &&
                        !end.isAfter(schedule.getEndTime())
        );

        if (!valid) {
            throw new RuntimeException("Selected time is outside provider availability.");
        }
    }

    @Transactional
    public void confirmAppointment(UUID appointmentId, User provider) {

        if (!provider.getRole().equals("PROVIDER")) {
            throw new RuntimeException("Only providers can confirm appointments.");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException("APPT-001", "Appointment not found."));

        if (!appointment.getEstablishment().getOwner().getId().equals(provider.getId())) {
            throw new RuntimeException("Unauthorized to confirm this appointment.");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new RuntimeException("Only pending appointments can be confirmed.");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
    }

    @Transactional
    public void cancelAppointment(UUID appointmentId, User requester) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException("APPT-001", "Appointment not found."));

        boolean isClient =
                appointment.getClient().getId().equals(requester.getId());

        boolean isProvider =
                appointment.getEstablishment()
                        .getOwner()
                        .getId()
                        .equals(requester.getId());

        if (!isClient && !isProvider) {
            throw new RuntimeException("Unauthorized to cancel this appointment.");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new AppException("APPT-002", "Appointment already cancelled.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
    }

    @Transactional
    public void rescheduleAppointment(UUID appointmentId,
                                      User requester,
                                      LocalDate newDate,
                                      LocalTime newStart) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found."));

        boolean isClient =
                appointment.getClient().getId().equals(requester.getId());

        boolean isProvider =
                appointment.getEstablishment()
                        .getOwner()
                        .getId()
                        .equals(requester.getId());

        if (!isClient && !isProvider) {
            throw new RuntimeException("Unauthorized to reschedule this appointment.");
        }

        // ❌ Status restrictions
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new RuntimeException("Completed appointments cannot be rescheduled.");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Cancelled appointments cannot be rescheduled.");
        }

        // ===============================
        // 🔒 CLIENT RESCHEDULING POLICY
        // ===============================
        if (isClient) {

            // ✅ Max reschedule limit
            int maxReschedules = 3;

            if (appointment.getRescheduleCount() >= maxReschedules) {
                throw new RuntimeException("Reschedule limit reached.");
            }

            // ✅ Cooldown (optional but recommended)
            if (appointment.getLastRescheduledAt() != null) {

                if (appointment.getLastRescheduledAt()
                        .isAfter(LocalDateTime.now().minusMinutes(30))) {

                    throw new RuntimeException(
                            "You can only reschedule once every 30 minutes."
                    );
                }
            }
        }

        Establishment establishment = appointment.getEstablishment();

        int duration = establishment.getSlotDurationMinutes();
        LocalTime newEnd = newStart.plusMinutes(duration);

        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        // ❌ Past date/time validation
        if (newDate.isBefore(today)) {
            throw new RuntimeException("Cannot reschedule to a past date.");
        }

        if (newDate.isEqual(today) && newStart.isBefore(nowTime)) {
            throw new RuntimeException("Cannot reschedule to a past time.");
        }

        // ⛔ Booking cutoff enforcement
        Integer cutoffHours = establishment.getBookingCutoffHours();

        if (cutoffHours != null) {

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime newDateTime = LocalDateTime.of(newDate, newStart);

            if (newDateTime.isBefore(now.plusHours(cutoffHours))) {
                throw new RuntimeException(
                        "Rescheduling is not allowed within " + cutoffHours + " hours of the appointment."
                );
            }
        }

        // ✅ Weekly availability validation
        validateWeeklyAvailability(establishment, newDate, newStart, newEnd);

        // ✅ Slot availability validation
        List<LocalTime> availableSlots =
                availabilityService.generateAvailableSlots(establishment, newDate);

        if (!availableSlots.contains(newStart)) {
            throw new RuntimeException("Selected slot is not available.");
        }

        // 🔄 Apply changes
        appointment.setAppointmentDate(newDate);
        appointment.setStartTime(newStart);
        appointment.setEndTime(newEnd);

        // 🔁 Reset status
        appointment.setStatus(AppointmentStatus.PENDING);

        // ===============================
        // 🔁 UPDATE RESCHEDULE TRACKING
        // ===============================
        if (isClient) {
            appointment.setRescheduleCount(
                    appointment.getRescheduleCount() + 1
            );
            appointment.setLastRescheduledAt(LocalDateTime.now());
        }
    }

    public List<Appointment> getAppointmentsForClient(User client) {
        return appointmentRepository.findByClient(client);
    }

    public List<Appointment> getAppointmentsForEstablishment(Establishment establishment) {
        return appointmentRepository.findByEstablishment(establishment);
    }

}