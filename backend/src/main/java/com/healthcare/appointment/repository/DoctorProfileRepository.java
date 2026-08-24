package com.healthcare.appointment.repository;

import com.healthcare.appointment.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import java.util.Optional;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {
    List<DoctorProfile> findBySpecializationContainingIgnoreCase(String specialization);
    Optional<DoctorProfile> findByUserId(Long userId);
}
