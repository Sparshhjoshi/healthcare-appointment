package com.healthcare.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompleteAppointmentDto {
    @NotBlank(message = "Doctor notes are required")
    private String doctorNotes;

    private String prescription;

    private java.util.List<MedicationDto> medications;
}
