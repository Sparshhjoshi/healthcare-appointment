package com.healthcare.appointment.controller;

import com.healthcare.appointment.dto.SymptomSubmissionDto;
import com.healthcare.appointment.entity.SymptomForm;
import com.healthcare.appointment.service.SymptomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/symptoms")
@RequiredArgsConstructor
public class SymptomController {

    private final SymptomService symptomService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitSymptoms(@Valid @RequestBody SymptomSubmissionDto dto) {
        try {
            SymptomForm form = symptomService.submitSymptoms(dto);
            return ResponseEntity.ok(form);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<?> getSymptomForm(@PathVariable Long appointmentId) {
        return symptomService.getSymptomFormByAppointment(appointmentId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
