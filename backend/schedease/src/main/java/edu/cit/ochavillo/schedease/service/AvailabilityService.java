package edu.cit.ochavillo.schedease.service;

import edu.cit.ochavillo.schedease.entity.Appointment;
import edu.cit.ochavillo.schedease.entity.Establishment;
import edu.cit.ochavillo.schedease.entity.User;
import edu.cit.ochavillo.schedease.entity.WeeklyAvailability;
import edu.cit.ochavillo.schedease.enums.AppointmentStatus;
import edu.cit.ochavillo.schedease.repository.AppointmentRepository;
import edu.cit.ochavillo.schedease.repository.WeeklyAvailabilityRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    private final WeeklyAvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;

    public AvailabilityService(
            WeeklyAvailabilityRepository availabilityRepository,
            AppointmentRepository appointmentRepository) {

        this.availabilityRepository = availabilityRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public void createWeeklyAvailability(User provider,
                                         Establishment establishment,
                                         DayOfWeek day,
                                         LocalTime start,
                                         LocalTime end) {

        if (!provider.getRole().equals("PROVIDER")) {
            throw new RuntimeException("Only providers can set availability.");
        }

        if (!establishment.getOwner().getId().equals(provider.getId())) {
            throw new RuntimeException("Unauthorized to modify this establishment.");
        }

        if (!start.isBefore(end)) {
            throw new RuntimeException("Start time must be before end time.");
        }

        validateNoOverlap(establishment, day, start, end);

        WeeklyAvailability availability = new WeeklyAvailability();
        availability.setEstablishment(establishment);
        availability.setDayOfWeek(day);
        availability.setStartTime(start);
        availability.setEndTime(end);

        availabilityRepository.save(availability);
    }

    private void validateNoOverlap(Establishment establishment,
                                   DayOfWeek day,
                                   LocalTime start,
                                   LocalTime end) {

        List<WeeklyAvailability> existing =
                availabilityRepository.findByEstablishmentAndDayOfWeek(establishment, day);

        boolean overlaps = existing.stream().anyMatch(e ->
                start.isBefore(e.getEndTime()) &&
                        end.isAfter(e.getStartTime())
        );

        if (overlaps) {
            throw new RuntimeException("Availability overlaps existing schedule.");
        }
    }

    public List<LocalTime> generateAvailableSlots(Establishment establishment, LocalDate date) {

        if (establishment.getSlotDurationMinutes() == null) {
            throw new RuntimeException("Establishment slot duration not configured.");
        }

        DayOfWeek day = date.getDayOfWeek();

        List<WeeklyAvailability> schedules =
                availabilityRepository.findByEstablishmentAndDayOfWeek(establishment, day);

        if (schedules.isEmpty()) {
            return List.of();
        }

        List<Appointment> existingAppointments =
                appointmentRepository
                        .findByEstablishsmentAndAppointmentDateAndStatusIn(
                                establishment,
                                date,
                                List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)
                        );

        Set<LocalTime> booked =
                existingAppointments.stream()
                        .map(Appointment::getStartTime)
                        .collect(Collectors.toSet());

        List<LocalTime> availableSlots = new ArrayList<>();
        int duration = establishment.getSlotDurationMinutes();

        for (WeeklyAvailability schedule : schedules) {

            LocalTime current = schedule.getStartTime();

            while (!current.plusMinutes(duration).isAfter(schedule.getEndTime())) {

                if (!booked.contains(current)) {
                    availableSlots.add(current);
                }

                current = current.plusMinutes(duration);
            }
        }

        return availableSlots;
    }
}