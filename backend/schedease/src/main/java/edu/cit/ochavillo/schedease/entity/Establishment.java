package edu.cit.ochavillo.schedease.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "establishments")
public class Establishment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "establishment_id")
    private Long Id;

    @OneToOne(optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String address;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Column(name = "slot_duration_minutes")
    private Integer slotDurationMinutes;

    @Column(name = "buffer_mintues")
    private Integer bufferMinutes;

    @Column(name = "booking_cutoff_hours")
    private Integer bookingCutoffHours;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
