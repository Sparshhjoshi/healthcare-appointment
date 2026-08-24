package com.healthcare.appointment.service;

import com.healthcare.appointment.dto.AppointmentRequestDto;
import com.healthcare.appointment.dto.CompleteAppointmentDto;
import com.healthcare.appointment.dto.MedicationDto;
import com.healthcare.appointment.entity.Appointment;
import com.healthcare.appointment.entity.AppointmentStatus;
import com.healthcare.appointment.entity.Medication;
import com.healthcare.appointment.entity.Role;
import com.healthcare.appointment.entity.User;
import com.healthcare.appointment.repository.AppointmentRepository;
import com.healthcare.appointment.repository.DoctorLeaveRepository;
import com.healthcare.appointment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DoctorLeaveRepository doctorLeaveRepository;
    private final NotificationService notificationService;
    private final GeminiAiService geminiAiService;
    private final com.healthcare.appointment.repository.MedicationRepository medicationRepository;
    private final GoogleCalendarService googleCalendarService;

    @Transactional
    public Appointment bookAppointment(AppointmentRequestDto dto) {
        // 1. Validate Patient
        User patient = userRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found!"));
        if (patient.getRole() != Role.PATIENT) {
            throw new RuntimeException("User is not a PATIENT!");
        }

        // 2. Validate Doctor
        User doctor = userRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found!"));
        if (doctor.getRole() != Role.DOCTOR) {
            throw new RuntimeException("User is not a DOCTOR!");
        }

        // 3. Check if Doctor is on Leave
        LocalDate requestedDate = dto.getAppointmentTime().toLocalDate();
        if (doctorLeaveRepository.existsByDoctorIdAndLeaveDate(dto.getDoctorId(), requestedDate)) {
            throw new RuntimeException("The doctor is on leave on this date. Please select another date.");
        }

        // 4. Double-Booking Check (Manual Check)
        boolean isConflict = appointmentRepository.existsByDoctorIdAndAppointmentTime(
                dto.getDoctorId(), 
                dto.getAppointmentTime()
        );
        if (isConflict) {
            throw new RuntimeException("This slot is already booked! Please choose another time.");
        }

        // 5. Generate Pre-Visit Summary using AI
        String preVisitSummary = geminiAiService.generatePreVisitSummary(dto.getSymptoms());

        // 6. Create and Save Appointment
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(dto.getAppointmentTime())
                .status(AppointmentStatus.BOOKED)
                .symptoms(dto.getSymptoms())
                .preVisitSummary(preVisitSummary)
                .build();

        // 7. Create Google Calendar Event
        googleCalendarService.createEvent(appointment).ifPresent(appointment::setGoogleCalendarEventId);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // 8. Send Mock Email and .ics Notification
        notificationService.sendBookingConfirmation(savedAppointment);

        return savedAppointment;
    }

    @Transactional
    public Appointment cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found!"));

        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new RuntimeException("Only BOOKED appointments can be cancelled.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        
        // Delete from Google Calendar
        if (appointment.getGoogleCalendarEventId() != null) {
            googleCalendarService.deleteEvent(appointment.getGoogleCalendarEventId());
            appointment.setGoogleCalendarEventId(null);
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Send cancellation email to both patient and doctor
        notificationService.sendCancellationEmail(savedAppointment);

        return savedAppointment;
    }

    public java.util.List<Appointment> getAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    public java.util.List<Appointment> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    @Transactional
    public Appointment completeAppointment(Long appointmentId, CompleteAppointmentDto dto) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found!"));
        
        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new RuntimeException("Only BOOKED appointments can be completed.");
        }

        // Generate Post-Visit Summary using AI
        String postVisitSummary = geminiAiService.generatePostVisitSummary(dto.getDoctorNotes());

        appointment.setDoctorNotes(dto.getDoctorNotes());
        appointment.setPrescription(dto.getPrescription());
        appointment.setPostVisitSummary(postVisitSummary);
        appointment.setStatus(AppointmentStatus.COMPLETED);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Save medications if any
        if (dto.getMedications() != null && !dto.getMedications().isEmpty()) {
            java.util.List<Medication> medsToSave = dto.getMedications().stream().map(medDto -> {
                Medication med = new Medication();
                med.setPatient(appointment.getPatient());
                med.setAppointment(savedAppointment);
                med.setName(medDto.getName());
                med.setFrequency(medDto.getFrequency());
                med.setStartDate(java.time.LocalDateTime.now());
                if (medDto.getDurationDays() > 0) {
                    med.setEndDate(java.time.LocalDateTime.now().plusDays(medDto.getDurationDays()));
                }
                return med;
            }).toList();
            medicationRepository.saveAll(medsToSave);
        }

        return savedAppointment;
    }
}
