package edu.cit.ochavillo.schedease.service;

import edu.cit.ochavillo.schedease.dto.CreateEstablishmentRequest;
import edu.cit.ochavillo.schedease.dto.UpdateEstablishmentRequest;
import edu.cit.ochavillo.schedease.entity.Establishment;
import edu.cit.ochavillo.schedease.entity.User;
import edu.cit.ochavillo.schedease.repository.EstablishmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstablishmentService {

    private final EstablishmentRepository establishmentRepository;

    public EstablishmentService(EstablishmentRepository establishmentRepository) {
        this.establishmentRepository = establishmentRepository;
    }

    // Browse establishments
    public List<Establishment> getEstablishments(String search) {

        if (search == null || search.isBlank()) {
            return establishmentRepository.findAll();
        }

        return establishmentRepository
                .findByNameContainingIgnoreCase(search);
    }

    // Get single establishment
    public Establishment getEstablishmentById(Long id) {

        return establishmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Establishment not found"));
    }

    // Create establishment
    @Transactional
    public Establishment createEstablishment(User provider,
                                             CreateEstablishmentRequest request) {

        if (!provider.getRole().equals("PROVIDER")) {
            throw new RuntimeException("Only providers can create establishments.");
        }

        Establishment establishment = new Establishment();
        establishment.setName(request.name());
        establishment.setDescription(request.description());
        establishment.setAddress(request.address());
        establishment.setContactEmail(request.contactEmail());
        establishment.setOwner(provider);

        return establishmentRepository.save(establishment);
    }

    // Update establishment
    @Transactional
    public void updateEstablishment(Long id,
                                    User provider,
                                    UpdateEstablishmentRequest request) {

        Establishment establishment = establishmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Establishment not found"));

        if (!establishment.getOwner().getId().equals(provider.getId())) {
            throw new RuntimeException("Unauthorized to update this establishment.");
        }

        establishment.setName(request.name());
        establishment.setDescription(request.description());
        establishment.setAddress(request.address());
        establishment.setContactEmail(request.contactEmail());
    }
}