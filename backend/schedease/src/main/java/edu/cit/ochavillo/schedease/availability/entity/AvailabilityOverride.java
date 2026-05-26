package edu.cit.ochavillo.schedease.availability.entity;

import edu.cit.ochavillo.schedease.establishment.entity.Establishment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "availability_overrides")
public class AvailabilityOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "override_date", nullable = false)
    private LocalDate overrideDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time",  nullable = false)
    private LocalTime endTime;

    @Column(name = "is_unavailable",  nullable = false)
    private boolean isUnavailable;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "establishment_id", nullable = false)
    private Establishment establishment;
}