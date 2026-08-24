package com.healthcare.appointment.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class LeaveRequestDto {
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Leave date is required")
    @FutureOrPresent(message = "Leave date cannot be in the past")
    private LocalDate leaveDate;

    private String reason;
}
