package edu.cit.ochavillo.schedease.repository;

import edu.cit.ochavillo.schedease.entity.Establishment;
import edu.cit.ochavillo.schedease.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface EstablishmentRepository extends JpaRepository<Establishment, Long> {
    Optional<Establishment> findById(Long id);
    Optional<Establishment> findByOwnerId(Long id);
    Optional<Establishment> findByOwner(User owner);
    boolean existsByName(String name);
    boolean existsByContactEmail(String email);
    List<Establishment> findByNameContainingIgnoreCase(String name);
    List<Establishment> findByIdGreaterThanOrderByIdAsc(Long id, Pageable pageable);
    List<Establishment> findByNameContainingIgnoreCaseAndIdGreaterThanOrderByIdAsc(String search, Long id, Pageable pageable);
}
