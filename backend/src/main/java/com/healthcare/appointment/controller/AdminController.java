package com.healthcare.appointment.controller;

import com.healthcare.appointment.dto.LeaveRequestDto;
import com.healthcare.appointment.entity.DoctorLeave;
import com.healthcare.appointment.service.AdminService;
import com.healthcare.appointment.service.DoctorService;
import com.healthcare.appointment.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;
    private final DoctorService doctorService;

    @PostMapping("/doctors")
    public ResponseEntity<?> createDoctor(@Valid @RequestBody com.healthcare.appointment.dto.CreateDoctorDto dto) {
        try {
            // 1. Create the User (Role = DOCTOR)
            com.healthcare.appointment.dto.AuthDto.RegisterRequest authReq = new com.healthcare.appointment.dto.AuthDto.RegisterRequest();
            authReq.setFirstName(dto.getFirstName());
            authReq.setLastName(dto.getLastName());
            authReq.setEmail(dto.getEmail());
            authReq.setPassword(dto.getPassword());
            authReq.setRole(com.healthcare.appointment.entity.Role.DOCTOR);
            
            com.healthcare.appointment.entity.User doctorUser = authService.register(authReq);

            // 2. Create the DoctorProfile
            com.healthcare.appointment.dto.DoctorProfileDto profileDto = new com.healthcare.appointment.dto.DoctorProfileDto();
            profileDto.setUserId(doctorUser.getId());
            profileDto.setSpecialization(dto.getSpecialization());
            profileDto.setWorkingHours(dto.getWorkingHours());
            profileDto.setSlotDuration(dto.getSlotDuration());

            com.healthcare.appointment.entity.DoctorProfile profile = doctorService.createDoctorProfile(profileDto);
            
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/doctors/{id}")
    public ResponseEntity<?> updateDoctor(@PathVariable Long id, @Valid @RequestBody com.healthcare.appointment.dto.DoctorProfileDto dto) {
        try {
            com.healthcare.appointment.entity.DoctorProfile profile = doctorService.updateDoctorProfile(id, dto);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/leaves")
    public ResponseEntity<?> addDoctorLeave(@Valid @RequestBody LeaveRequestDto dto) {
        try {
            DoctorLeave leave = adminService.addDoctorLeave(dto);
            return ResponseEntity.ok(leave);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/leaves")
    public ResponseEntity<java.util.List<DoctorLeave>> getAllLeaves() {
        return ResponseEntity.ok(adminService.getAllLeaves());
    }

    @DeleteMapping("/leaves/{id}")
    public ResponseEntity<?> deleteLeave(@PathVariable Long id) {
        try {
            adminService.deleteLeave(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
