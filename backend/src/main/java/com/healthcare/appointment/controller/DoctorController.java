package com.healthcare.appointment.controller;

import com.healthcare.appointment.dto.DoctorProfileDto;
import com.healthcare.appointment.entity.DoctorProfile;
import com.healthcare.appointment.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    // Create a new doctor profile
    @PostMapping
    public ResponseEntity<?> createProfile(@Valid @RequestBody DoctorProfileDto dto) {
        try {
            DoctorProfile profile = doctorService.createDoctorProfile(dto);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get a list of all doctors (for patients to search)
    @GetMapping
    public ResponseEntity<List<DoctorProfile>> getAllDoctors(@RequestParam(required = false) String specialization) {
        return ResponseEntity.ok(doctorService.getAllDoctors(specialization));
    }

    // Get available slots for a specific doctor on a specific date
    @GetMapping("/{id}/slots")
    public ResponseEntity<?> getDoctorSlots(@PathVariable Long id, @RequestParam String date) {
        try {
            java.time.LocalDate requestedDate = java.time.LocalDate.parse(date);
            List<String> slots = doctorService.getAvailableSlots(id, requestedDate);
            return ResponseEntity.ok(slots);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
