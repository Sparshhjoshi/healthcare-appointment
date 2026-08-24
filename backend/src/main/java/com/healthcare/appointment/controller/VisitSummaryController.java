package com.healthcare.appointment.controller;

import com.healthcare.appointment.dto.DoctorNotesDto;
import com.healthcare.appointment.entity.VisitSummary;
import com.healthcare.appointment.service.VisitSummaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
public class VisitSummaryController {

    private final VisitSummaryService visitSummaryService;

    @PostMapping("/notes")
    public ResponseEntity<?> submitDoctorNotes(@Valid @RequestBody DoctorNotesDto dto) {
        try {
            VisitSummary summary = visitSummaryService.submitDoctorNotes(dto);
            return ResponseEntity.ok(summary);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
