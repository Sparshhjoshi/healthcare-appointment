package com.healthcare.appointment.service;

import com.healthcare.appointment.dto.DoctorNotesDto;
import com.healthcare.appointment.entity.Appointment;
import com.healthcare.appointment.entity.VisitSummary;
import com.healthcare.appointment.repository.AppointmentRepository;
import com.healthcare.appointment.repository.VisitSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VisitSummaryService {

    private final VisitSummaryRepository visitSummaryRepository;
    private final AppointmentRepository appointmentRepository;
    private final GeminiAiService aiService;

    public VisitSummary submitDoctorNotes(DoctorNotesDto dto) {
        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found!"));

        // 1. Call Gemini to convert technical notes into a patient-friendly summary
        String aiResponse = aiService.generatePostVisitSummary(dto.getClinicalNotes());

        // 2. Save the doctor's inputs and the AI generated summary
        VisitSummary summary = VisitSummary.builder()
                .appointment(appointment)
                .clinicalNotes(dto.getClinicalNotes())
                .prescription(dto.getPrescription())
                .postVisitSummary(aiResponse)
                .build();

        return visitSummaryRepository.save(summary);
    }
}
