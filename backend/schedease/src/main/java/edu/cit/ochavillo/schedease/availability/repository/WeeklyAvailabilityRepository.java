package edu.cit.ochavillo.schedease.availability.repository;

import edu.cit.ochavillo.schedease.establishment.entity.Establishment;
import edu.cit.ochavillo.schedease.availability.entity.WeeklyAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

public interface WeeklyAvailabilityRepository
        extends JpaRepository<WeeklyAvailability, UUID> {

    List<WeeklyAvailability>
    findByEstablishmentAndDayOfWeek(Establishment establishment, DayOfWeek dayOfWeek);

    void deleteAllByEstablishment(Establishment establishment);

    List<WeeklyAvailability> findAllByEstablishment(Establishment establishment);
}