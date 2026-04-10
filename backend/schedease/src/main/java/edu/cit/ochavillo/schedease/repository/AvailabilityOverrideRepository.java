package edu.cit.ochavillo.schedease.repository;

import edu.cit.ochavillo.schedease.entity.AvailabilityOverride;
import edu.cit.ochavillo.schedease.entity.Establishment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AvailabilityOverrideRepository
        extends JpaRepository<AvailabilityOverride, Long> {

    Optional<AvailabilityOverride> findByEstablishmentAndOverrideDate(
            Establishment establishment,
            LocalDate overrideDate
    );

    List<AvailabilityOverride> findByEstablishment(Establishment establishment);
}
