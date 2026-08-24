package com.healthcare.appointment.service;

import com.healthcare.appointment.dto.LeaveRequestDto;
import com.healthcare.appointment.entity.DoctorLeave;
import com.healthcare.appointment.entity.Role;
import com.healthcare.appointment.entity.User;
import com.healthcare.appointment.repository.DoctorLeaveRepository;
import com.healthcare.appointment.repository.UserRepository;
import com.healthcare.appointment.repository.AppointmentRepository;
import com.healthcare.appointment.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final DoctorLeaveRepository doctorLeaveRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;

    public DoctorLeave addDoctorLeave(LeaveRequestDto dto) {
        User doctor = userRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found!"));

        if (doctor.getRole() != Role.DOCTOR) {
            throw new RuntimeException("User is not a DOCTOR!");
        }

        java.time.LocalDate leaveDate = dto.getLeaveDate();

        if (doctorLeaveRepository.existsByDoctorIdAndLeaveDate(dto.getDoctorId(), leaveDate)) {
            throw new RuntimeException("Doctor is already on leave for this date!");
        }

        // --- Handle conflicts ---
        // 1. Find any appointments for this doctor on this date
        java.time.LocalDateTime startOfDay = leaveDate.atStartOfDay();
        java.time.LocalDateTime endOfDay = leaveDate.atTime(23, 59, 59);

        java.util.List<com.healthcare.appointment.entity.Appointment> conflicts = 
            appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(dto.getDoctorId(), startOfDay, endOfDay);
        
        for (com.healthcare.appointment.entity.Appointment appt : conflicts) {
            appt.setStatus(com.healthcare.appointment.entity.AppointmentStatus.CANCELLED);
            appointmentRepository.save(appt);
            
            // Send cancellation email
            notificationService.sendCancellationEmail(appt);
        }

        DoctorLeave leave = DoctorLeave.builder()
                .doctor(doctor)
                .leaveDate(leaveDate)
                .reason(dto.getReason())
                .build();

        return doctorLeaveRepository.save(leave);
    }

    public java.util.List<DoctorLeave> getAllLeaves() {
        return doctorLeaveRepository.findAll();
    }

    public void deleteLeave(Long id) {
        if (!doctorLeaveRepository.existsById(id)) {
            throw new RuntimeException("Leave record not found!");
        }
        doctorLeaveRepository.deleteById(id);
    }
}
