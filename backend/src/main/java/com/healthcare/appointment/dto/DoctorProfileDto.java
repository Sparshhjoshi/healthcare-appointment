package com.healthcare.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DoctorProfileDto {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    @NotBlank(message = "Working hours are required (e.g. 09:00-17:00)")
    private String workingHours;

    @NotNull(message = "Slot duration is required")
    private Integer slotDuration;
}
