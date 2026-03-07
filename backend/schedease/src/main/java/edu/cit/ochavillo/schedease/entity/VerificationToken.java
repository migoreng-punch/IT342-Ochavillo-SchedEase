package edu.cit.ochavillo.schedease.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "verification_token")
@Getter
@Setter
@NoArgsConstructor
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name= "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "last_sent_at")
    private Instant lastSentAt;

    @Column(name= "used")
    private boolean used = false;
}
