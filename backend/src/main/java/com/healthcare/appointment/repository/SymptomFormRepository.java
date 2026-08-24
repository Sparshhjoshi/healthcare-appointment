package com.healthcare.appointment.repository;

import com.healthcare.appointment.entity.SymptomForm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SymptomFormRepository extends JpaRepository<SymptomForm, Long> {
    Optional<SymptomForm> findByAppointmentId(Long appointmentId);
}
