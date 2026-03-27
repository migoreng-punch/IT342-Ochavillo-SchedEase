package edu.cit.ochavillo.schedease.service;

import edu.cit.ochavillo.schedease.dto.*;
import edu.cit.ochavillo.schedease.entity.Establishment;
import edu.cit.ochavillo.schedease.entity.User;
import edu.cit.ochavillo.schedease.repository.EstablishmentRepository;
import edu.cit.ochavillo.schedease.util.AppException;
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
    public List<EstablishmentDTO> getEstablishments(String search) {

        List<Establishment> establishments;

        if (search == null || search.isBlank()) {
            establishments = establishmentRepository.findAll();
        }else{
            establishments = establishmentRepository
                    .findByNameContainingIgnoreCase(search);
        }

        return establishments.stream()
                .map(this::convertToDTO)
                .toList();
    }

    // Get single establishment
    public EstablishmentDTO getEstablishmentById(Long id) {

        Establishment establishment = establishmentRepository.findById(id)
                .orElseThrow(() -> new AppException("ESTAB-001", "Establishment not found"));

        return convertToDTO(establishment);
    }

    // Create establishment
    @Transactional
    public EstablishmentDTO createEstablishment(User provider,
                                                           CreateEstablishmentRequest request) {

        Establishment establishment = new Establishment();
        establishment.setName(request.name());
        establishment.setDescription(request.description());
        establishment.setAddress(request.address());
        establishment.setContactEmail(request.contactEmail());
        establishment.setSlotDurationMinutes(request.slotDurationMinutes());
        establishment.setOwner(provider);
        establishmentRepository.save(establishment);

        return convertToDTO(establishment);
    }

    // Update establishment
    @Transactional
    public EstablishmentDTO updateEstablishment(Long id,
                                    User provider,
                                    UpdateEstablishmentRequest request) {

        Establishment establishment = establishmentRepository.findById(id)
                .orElseThrow(() -> new AppException("ESTAB-001", "Establishment not found"));

        establishment.setName(request.name());
        establishment.setDescription(request.description());
        establishment.setAddress(request.address());
        establishment.setContactEmail(request.contactEmail());
        establishment.setSlotDurationMinutes(request.slotDurationMinutes());
        establishment.setBufferMinutes(request.bufferMinutes());
        establishment.setBookingCutoffHours(request.bookingCutoffHours());

        establishmentRepository.save(establishment);

        return convertToDTO(establishment);
    }

    private EstablishmentDTO convertToDTO(Establishment entity) {
        return new EstablishmentDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getAddress(),
                entity.getContactEmail(),
                entity.getSlotDurationMinutes(),
                entity.getBufferMinutes(),
                entity.getBookingCutoffHours(),
                entity.getOwner().getId(),
                entity.getOwner().getFirstName()
        );
    }
}