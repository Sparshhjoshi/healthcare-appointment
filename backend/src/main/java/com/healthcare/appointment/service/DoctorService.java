package com.healthcare.appointment.service;

import com.healthcare.appointment.dto.DoctorProfileDto;
import com.healthcare.appointment.entity.DoctorProfile;
import com.healthcare.appointment.entity.Role;
import com.healthcare.appointment.entity.User;
import com.healthcare.appointment.repository.DoctorProfileRepository;
import com.healthcare.appointment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.healthcare.appointment.repository.AppointmentRepository;
import com.healthcare.appointment.repository.DoctorLeaveRepository;
import com.healthcare.appointment.entity.Appointment;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorLeaveRepository doctorLeaveRepository;

    public DoctorProfile createDoctorProfile(DoctorProfileDto dto) {
        // 1. Find the user by ID
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found!"));
                
        // 2. Ensure they are actually a doctor
        if (user.getRole() != Role.DOCTOR) {
            throw new RuntimeException("This user is not registered as a DOCTOR!");
        }

        // 3. Create the profile
        DoctorProfile profile = DoctorProfile.builder()
                .user(user)
                .specialization(dto.getSpecialization())
                .workingHours(dto.getWorkingHours())
                .slotDuration(dto.getSlotDuration())
                .build();

        return doctorProfileRepository.save(profile);
    }

    public DoctorProfile updateDoctorProfile(Long userId, DoctorProfileDto dto) {
        DoctorProfile profile = doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found!"));
        
        if (dto.getSpecialization() != null && !dto.getSpecialization().isEmpty()) {
            profile.setSpecialization(dto.getSpecialization());
        }
        if (dto.getWorkingHours() != null && !dto.getWorkingHours().isEmpty()) {
            // Basic validation
            if (dto.getWorkingHours().split("-").length != 2) {
                throw new RuntimeException("Invalid working hours format. Expected HH:mm-HH:mm");
            }
            profile.setWorkingHours(dto.getWorkingHours());
        }
        if (dto.getSlotDuration() != null && dto.getSlotDuration() > 0) {
            profile.setSlotDuration(dto.getSlotDuration());
        }

        return doctorProfileRepository.save(profile);
    }

    public List<DoctorProfile> getAllDoctors(String specialization) {
        if (specialization != null && !specialization.trim().isEmpty()) {
            return doctorProfileRepository.findBySpecializationContainingIgnoreCase(specialization);
        }
        return doctorProfileRepository.findAll();
    }

    public List<String> getAvailableSlots(Long doctorId, LocalDate date) {
        // 1. Check if doctor is on leave
        if (doctorLeaveRepository.existsByDoctorIdAndLeaveDate(doctorId, date)) {
            return new ArrayList<>(); // Empty list if on leave
        }

        // 2. Get Doctor Profile
        DoctorProfile profile = doctorProfileRepository.findByUserId(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found!"));

        // Parse working hours (Expected format: "09:00-17:00")
        String[] hours = profile.getWorkingHours().split("-");
        if (hours.length != 2) {
            throw new RuntimeException("Invalid working hours format. Expected HH:mm-HH:mm");
        }
        
        LocalTime startTime = LocalTime.parse(hours[0].trim());
        LocalTime endTime = LocalTime.parse(hours[1].trim());
        int slotDuration = profile.getSlotDuration();

        // 3. Get existing appointments
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        List<Appointment> existingAppts = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);
        
        // Extract booked times
        List<LocalTime> bookedTimes = existingAppts.stream()
                .filter(a -> !com.healthcare.appointment.entity.AppointmentStatus.CANCELLED.equals(a.getStatus()))
                .map(a -> a.getAppointmentTime().toLocalTime())
                .toList();

        // 4. Generate Slots
        List<String> availableSlots = new ArrayList<>();
        LocalTime currentSlot = startTime;

        while (currentSlot.plusMinutes(slotDuration).isBefore(endTime) || currentSlot.plusMinutes(slotDuration).equals(endTime)) {
            if (!bookedTimes.contains(currentSlot)) {
                availableSlots.add(currentSlot.toString());
            }
            currentSlot = currentSlot.plusMinutes(slotDuration);
        }

        return availableSlots;
    }
}
