package edu.cit.ochavillo.schedease.repository;

import edu.cit.ochavillo.schedease.entity.Establishment;
import edu.cit.ochavillo.schedease.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstablishmentRepository extends JpaRepository<Establishment, Long> {
    Optional<Establishment> findById(Long id);
    Optional<Establishment> findByOwnerId(Long id);
    Optional<Establishment> findByOwner(User owner);
    boolean existsByName(String name);
    boolean existsByContactEmail(String email);
}
