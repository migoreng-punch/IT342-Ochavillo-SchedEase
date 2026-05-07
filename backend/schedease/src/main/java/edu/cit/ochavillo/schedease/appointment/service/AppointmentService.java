package edu.cit.ochavillo.schedease.appointment.service;

import edu.cit.ochavillo.schedease.appointment.dto.AppointmentDTO;
import edu.cit.ochavillo.schedease.appointment.entity.Appointment;
import edu.cit.ochavillo.schedease.establishment.entity.Establishment;
import edu.cit.ochavillo.schedease.user.entity.User;
import edu.cit.ochavillo.schedease.availability.entity.WeeklyAvailability;
import edu.cit.ochavillo.schedease.appointment.enums.AppointmentStatus;
import edu.cit.ochavillo.schedease.appointment.repository.AppointmentRepository;
import edu.cit.ochavillo.schedease.availability.repository.WeeklyAvailabilityRepository;
import edu.cit.ochavillo.schedease.availability.service.AvailabilityService;
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
    public AppointmentDTO bookAppointment(User client,
                                Establishment establishment,
                                LocalDate date,
                                LocalTime start) {

        List<LocalTime> validSlots = availabilityService.generateAvailableSlots(establishment, date);

        int duration = establishment.getSlotDurationMinutes();
        LocalTime end = start.plusMinutes(duration);

        if (!validSlots.contains(start)) {
            throw new AppException("AVAIL-005", "Selected slot is not available.");
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

        return convertToDTO(appointment);
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
            throw new AppException("AVAIL-006", "Selected time is outside provider availability.");
        }
    }

    @Transactional
    public AppointmentDTO confirmAppointment(UUID appointmentId, User provider) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException("APPT-001", "Appointment not found."));

        if (!appointment.getEstablishment().getOwner().getId().equals(provider.getId())) {
            throw new AppException("AUTH-005", "Unauthorized to confirm this appointment.");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new AppException("APPT-07", "Only pending appointments can be confirmed.");
        }
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        appointmentRepository.save(appointment);

        return(convertToDTO(appointment));
    }

    @Transactional
    public AppointmentDTO cancelAppointment(UUID appointmentId, User requester) {

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
            throw new AppException("AUTH-005", "Unauthorized to confirm this appointment.");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new AppException("APPT-002", "Appointment already cancelled.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);

        appointmentRepository.save(appointment);

        return(convertToDTO(appointment));
    }

    @Transactional
    public AppointmentDTO rescheduleAppointment(UUID appointmentId,
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
            throw new AppException("AUTH-005", "Unauthorized to reschedule this appointment.");
        }

        // ❌ Status restrictions
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new AppException("APPT-007", "Completed appointments cannot be rescheduled.");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new AppException("APPT-007", "Cancelled appointments cannot be rescheduled.");
        }

        // ===============================
        // 🔒 CLIENT RESCHEDULING POLICY
        // ===============================
        if (isClient) {

            // ✅ Max reschedule limit
            int maxReschedules = 3;

            if (appointment.getRescheduleCount() >= maxReschedules) {
                throw new AppException("RESCHED-001", "Reschedule limit reached.");
            }

            // ✅ Cooldown (optional but recommended)
            if (appointment.getLastRescheduledAt() != null) {

                if (appointment.getLastRescheduledAt()
                        .isAfter(LocalDateTime.now().minusMinutes(30))) {

                    throw new AppException("RESCHED-002",
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
            throw new AppException("RESCHED-003", "Cannot reschedule to a past date.");
        }

        if (newDate.isEqual(today) && newStart.isBefore(nowTime)) {
            throw new AppException("RESCHED-003", "Cannot reschedule to a past time.");
        }

        // ⛔ Booking cutoff enforcement
        Integer cutoffHours = establishment.getBookingCutoffHours();

        if (cutoffHours != null) {

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime newDateTime = LocalDateTime.of(newDate, newStart);

            if (newDateTime.isBefore(now.plusHours(cutoffHours))) {
                throw new AppException("RESCHED-004",
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
            throw new AppException("AVAIL-005","Selected slot is not available.");
        }

        // 🔄 Apply changes
        appointment.setAppointmentDate(newDate);
        appointment.setStartTime(newStart);
        appointment.setEndTime(newEnd);

        // 🔁 Reset status
        appointment.setStatus(AppointmentStatus.PENDING);
        appointmentRepository.save(appointment);

        // ===============================
        // 🔁 UPDATE RESCHEDULE TRACKING
        // ===============================
        if (isClient) {
            appointment.setRescheduleCount(
                    appointment.getRescheduleCount() + 1
            );
            appointment.setLastRescheduledAt(LocalDateTime.now());
        }
        return (convertToDTO(appointment));
    }

    public List<AppointmentDTO> getAppointmentsForClient(User client) {

        List<Appointment> appointments = appointmentRepository.findByClient(client);

        return appointments.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<AppointmentDTO> getAppointmentsForEstablishment(Establishment establishment) {

        List<Appointment> appointments = appointmentRepository.findByEstablishment(establishment);

        return appointments.stream()
                .map(this::convertToDTO)
                .toList();
    }

    private AppointmentDTO convertToDTO(Appointment appointment) {
        return new AppointmentDTO(
                appointment.getId(),
                appointment.getClient().getId(),
                appointment.getEstablishment().getId(),
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus()
        );
    }

}