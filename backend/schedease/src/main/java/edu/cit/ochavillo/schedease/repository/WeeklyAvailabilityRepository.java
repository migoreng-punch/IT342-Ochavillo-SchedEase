package edu.cit.ochavillo.schedease.repository;

import edu.cit.ochavillo.schedease.entity.Establishment;
import edu.cit.ochavillo.schedease.entity.WeeklyAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

public interface WeeklyAvailabilityRepository
        extends JpaRepository<WeeklyAvailability, UUID> {

    List<WeeklyAvailability>
    findByEstablishmentAndDayOfWeek(Establishment establishment, DayOfWeek dayOfWeek);
}