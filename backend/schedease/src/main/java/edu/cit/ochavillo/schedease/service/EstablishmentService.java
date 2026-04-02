package edu.cit.ochavillo.schedease.service;

import edu.cit.ochavillo.schedease.dto.*;
import edu.cit.ochavillo.schedease.entity.Establishment;
import edu.cit.ochavillo.schedease.entity.User;
import edu.cit.ochavillo.schedease.repository.EstablishmentRepository;
import edu.cit.ochavillo.schedease.util.AppException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Service
public class EstablishmentService {

    private final EstablishmentRepository establishmentRepository;

    public EstablishmentService(EstablishmentRepository establishmentRepository) {
        this.establishmentRepository = establishmentRepository;
    }

    // Browse establishments
    public CursorResponse<EstablishmentDTO> getEstablishments(String search, Long cursor, int limit) {

        // 1. If cursor is null (first page), start at ID 0.
        Long actualCursor = (cursor != null) ? cursor : 0L;

        // 2. Set the SQL Limit (We always ask for "page 0" relative to our cursor)
        Pageable pageable = PageRequest.of(0, limit);

        List<Establishment> establishments;

        // 3. Execute the correct query
        if (search == null || search.isBlank()) {
            establishments = establishmentRepository
                    .findByIdGreaterThanOrderByIdAsc(actualCursor, pageable);
        } else {
            establishments = establishmentRepository
                    .findByNameContainingIgnoreCaseAndIdGreaterThanOrderByIdAsc(search, actualCursor, pageable);
        }

        // 4. Convert to DTOs
        List<EstablishmentDTO> dtos = establishments.stream()
                .map(this::convertToDTO) // Or EstablishmentDTO::fromEntity
                .toList();

        // 5. Calculate pagination metadata
        // The next cursor is simply the ID of the last item in this list
        Long nextCursor = dtos.isEmpty() ? null : dtos.get(dtos.size() - 1).id();

        // If we asked for 10 items and got 10, there are probably more!
        boolean hasMore = dtos.size() == limit;

        // 6. Return the packaged response
        return new CursorResponse<>(dtos, nextCursor, hasMore);
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

        if (!establishment.getOwner().getId().equals(provider.getId())) {
            throw new AppException("AUTH-005", "You do not have permission to edit this establishment.");
        }

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