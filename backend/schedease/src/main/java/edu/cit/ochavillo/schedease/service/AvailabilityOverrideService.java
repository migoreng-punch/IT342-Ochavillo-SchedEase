package edu.cit.ochavillo.schedease.service;

import edu.cit.ochavillo.schedease.dto.AvailabilityOverrideDTO;
import edu.cit.ochavillo.schedease.entity.AvailabilityOverride;
import edu.cit.ochavillo.schedease.entity.Establishment;
import edu.cit.ochavillo.schedease.repository.AvailabilityOverrideRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AvailabilityOverrideService {

    private final AvailabilityOverrideRepository overrideRepository;

    public AvailabilityOverrideService(AvailabilityOverrideRepository overrideRepository) {
        this.overrideRepository = overrideRepository;
    }

    // 🏗 Create or Update Override
    @Transactional
    public AvailabilityOverrideDTO setOverride(Establishment establishment,
                            LocalDate date,
                            LocalTime start,
                            LocalTime end,
                            boolean unavailable) {

        AvailabilityOverride override =
                overrideRepository.findByEstablishmentAndOverrideDate(establishment, date)
                        .orElse(new AvailabilityOverride());

        override.setEstablishment(establishment);
        override.setOverrideDate(date);
        override.setUnavailable(unavailable);

        if (unavailable) {
            override.setStartTime(null);
            override.setEndTime(null);
        } else {

            if (start == null || end == null) {
                throw new RuntimeException("Start and end time required.");
            }

            if (!start.isBefore(end)) {
                throw new RuntimeException("Start time must be before end time.");
            }

            override.setStartTime(start);
            override.setEndTime(end);
        }

        overrideRepository.save(override);

        return convertToDTO(override);
    }

    // 🔎 Get Override for a Date
    public Optional<AvailabilityOverrideDTO> getOverride(
            Establishment establishment,
            LocalDate date) {

        return overrideRepository.findByEstablishmentAndOverrideDate(establishment, date)
                .map(this::convertToDTO);
    }

    // ❌ Delete Override (Optional)
    @Transactional
    public void deleteOverride(Long id, Establishment establishment) {

        AvailabilityOverride override = overrideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Override not found"));

        if (!override.getEstablishment().getId()
                .equals(establishment.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        overrideRepository.delete(override);
    }

    public List<AvailabilityOverrideDTO> getAllOverrides(Establishment establishment) {

        List<AvailabilityOverride> overrides = overrideRepository.findByEstablishment(establishment);

        return overrides.stream()
                .map(this::convertToDTO)
                .toList();
    }

    private AvailabilityOverrideDTO convertToDTO(AvailabilityOverride override){
        return new AvailabilityOverrideDTO(
                override.getId(),
                override.getOverrideDate(),
                override.getStartTime(),
                override.getEndTime(),
                override.isUnavailable(),
                override.getEstablishment().getId()
        );
    }
}
