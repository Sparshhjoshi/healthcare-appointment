package com.healthcare.appointment.service;

import com.healthcare.appointment.dto.SymptomSubmissionDto;
import com.healthcare.appointment.entity.Appointment;
import com.healthcare.appointment.entity.SymptomForm;
import com.healthcare.appointment.repository.AppointmentRepository;
import com.healthcare.appointment.repository.SymptomFormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SymptomService {

    private final SymptomFormRepository symptomFormRepository;
    private final AppointmentRepository appointmentRepository;
    private final GeminiAiService aiService;

    public SymptomForm submitSymptoms(SymptomSubmissionDto dto) {
        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found!"));

        // 1. Call Gemini to get the AI summary
        String aiResponse = aiService.generatePreVisitSummary(dto.getSymptoms());

        // 2. Check if a form already exists for this appointment
        Optional<SymptomForm> existingForm = symptomFormRepository.findByAppointmentId(dto.getAppointmentId());

        SymptomForm form;
        if (existingForm.isPresent()) {
            // Update existing form so we don't get a duplicate key error!
            form = existingForm.get();
            form.setPatientSymptoms(dto.getSymptoms());
            form.setChiefComplaint(aiResponse);
        } else {
            // Create a new form
            form = SymptomForm.builder()
                    .appointment(appointment)
                    .patientSymptoms(dto.getSymptoms())
                    .chiefComplaint(aiResponse) 
                    .build();
        }

        return symptomFormRepository.save(form);
    }

    public Optional<SymptomForm> getSymptomFormByAppointment(Long appointmentId) {
        return symptomFormRepository.findByAppointmentId(appointmentId);
    }
}
