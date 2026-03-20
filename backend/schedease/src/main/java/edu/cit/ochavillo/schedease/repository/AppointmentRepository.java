package edu.cit.ochavillo.schedease.repository;

import edu.cit.ochavillo.schedease.entity.Appointment;
import edu.cit.ochavillo.schedease.entity.Establishment;
import edu.cit.ochavillo.schedease.entity.User;
import edu.cit.ochavillo.schedease.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository
        extends JpaRepository<Appointment, UUID> {

    List<Appointment>
    findByEstablishmentAndAppointmentDate(Establishment establishment, LocalDate date);

    List<Appointment>
    findByClient(User client);

    List<Appointment>
    findByEstablishment(Establishment establishment);

    List<Appointment> findByEstablishmentAndAppointmentDateAndStatusIn(
            Establishment establishment,
            LocalDate date,
            List<AppointmentStatus> statuses
    );
}
