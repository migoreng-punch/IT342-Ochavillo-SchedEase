package edu.cit.ochavillo.schedease.availability.service;

import edu.cit.ochavillo.schedease.appointment.entity.Appointment;
import edu.cit.ochavillo.schedease.availability.entity.AvailabilityOverride;
import edu.cit.ochavillo.schedease.availability.entity.WeeklyAvailability;
import edu.cit.ochavillo.schedease.appointment.enums.AppointmentStatus;
import edu.cit.ochavillo.schedease.appointment.repository.AppointmentRepository;
import edu.cit.ochavillo.schedease.availability.repository.AvailabilityOverrideRepository;
import edu.cit.ochavillo.schedease.availability.repository.WeeklyAvailabilityRepository;
import edu.cit.ochavillo.schedease.establishment.entity.Establishment;
import edu.cit.ochavillo.schedease.user.entity.User;
import edu.cit.ochavillo.schedease.util.AppException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    private final WeeklyAvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final AvailabilityOverrideRepository availabilityOverrideRepository;

    public AvailabilityService(
            WeeklyAvailabilityRepository availabilityRepository,
            AppointmentRepository appointmentRepository,
            AvailabilityOverrideRepository availabilityOverrideRepository) {

        this.availabilityRepository = availabilityRepository;
        this.appointmentRepository = appointmentRepository;
        this.availabilityOverrideRepository = availabilityOverrideRepository;
    }

    @Transactional
    public void createWeeklyAvailability(User provider,
                                         Establishment establishment,
                                         DayOfWeek day,
                                         LocalTime start,
                                         LocalTime end) {

        if (!establishment.getOwner().getId().equals(provider.getId())) {
            throw new AppException("AUTH-005", "Unauthorized to modify this establishment.");
        }

        if (!start.isBefore(end)) {
            throw new AppException("AVAIL-004", "Start time must be before end time.");
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
            throw new AppException("AVAIL-002", "Availability overlaps existing schedule.");
        }
    }

    public List<LocalTime> generateAvailableSlots(Establishment establishment, LocalDate date) {

        if (establishment.getSlotDurationMinutes() == null) {
            throw new AppException("AVAIL-003", "Establishment slot duration not configured.");
        }

        DayOfWeek day = date.getDayOfWeek();
        System.out.println("1. Looking for day: " + day.name());

        List<WeeklyAvailability> schedules =
                availabilityRepository.findByEstablishmentAndDayOfWeek(establishment, day);

        System.out.println("2. Found schedules in DB: " + schedules.size());

        Optional<AvailabilityOverride> overrideOpt =
                availabilityOverrideRepository.findByEstablishmentAndOverrideDate(establishment, date);

        if (overrideOpt.isPresent()) {
            System.out.println("🚨 OVERRIDE FOUND for date: " + date);
            AvailabilityOverride override = overrideOpt.get();

            // ❌ Fully unavailable
            if (override.isUnavailable()) {
                System.out.println("🚨 OVERRIDE ACTION: Establishment is marked as FULLY CLOSED today.");
                return List.of();
            }

            // ✅ Custom hours
            System.out.println("🚨 OVERRIDE ACTION: Changing schedule to custom hours -> "
                    + override.getStartTime() + " to " + override.getEndTime());
            WeeklyAvailability temp = new WeeklyAvailability();
            temp.setStartTime(override.getStartTime());
            temp.setEndTime(override.getEndTime());

            schedules = List.of(temp);
        } else{
            System.out.println("No Availability Override");
        }


        if (schedules.isEmpty()) {
            System.out.println("3. Bailing out because schedules is EMPTY!");
            return List.of();
        }



        List<Appointment> existingAppointments =
                appointmentRepository
                        .findByEstablishmentAndAppointmentDateAndStatusIn(
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

    @Transactional // REQUIRED for database modifications (Deletes/Updates)
    public void deleteAllByProviderAndEstablishment(Establishment establishment) {
        availabilityRepository.deleteAllByEstablishment(establishment);
    }

    // 🚨 NEW: Retrieve the schedule
    public List<WeeklyAvailability> getWeeklyAvailability(Establishment establishment) {
        return availabilityRepository.findAllByEstablishment(establishment);
    }


}