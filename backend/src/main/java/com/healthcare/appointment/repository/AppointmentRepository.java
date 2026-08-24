package com.healthcare.appointment.repository;

import com.healthcare.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // Find all appointments for a specific patient
    List<Appointment> findByPatientId(Long patientId);
    
    // Find all appointments for a specific doctor
    List<Appointment> findByDoctorId(Long doctorId);

    // Check if a doctor already has an appointment at a specific time
    boolean existsByDoctorIdAndAppointmentTime(Long doctorId, LocalDateTime appointmentTime);

    // Find appointments within a time range
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);
}
