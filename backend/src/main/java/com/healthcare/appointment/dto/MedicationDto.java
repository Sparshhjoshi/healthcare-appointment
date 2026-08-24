package com.healthcare.appointment.dto;

import lombok.Data;

@Data
public class MedicationDto {
    private String name;
    private String frequency;
    private int durationDays;
}
