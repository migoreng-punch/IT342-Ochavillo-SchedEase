package edu.cit.ochavillo.schedease.user.repository;


import edu.cit.ochavillo.schedease.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository <User, Long>{
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    void deleteByEnabledFalseAndCreatedAtBefore(Instant date);
}