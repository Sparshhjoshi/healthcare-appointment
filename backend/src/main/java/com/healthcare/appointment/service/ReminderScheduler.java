package com.healthcare.appointment.service;

import com.healthcare.appointment.entity.Appointment;
import com.healthcare.appointment.entity.AppointmentStatus;
import com.healthcare.appointment.entity.Medication;
import com.healthcare.appointment.repository.AppointmentRepository;
import com.healthcare.appointment.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final MedicationRepository medicationRepository;
    private final NotificationService notificationService;

    // Run every day at 8 AM to send medication reminders and appointment reminders
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyReminders() {
        System.out.println("Running daily reminder scheduler...");

        // 1. Appointment Reminders for tomorrow
        LocalDateTime tomorrowStart = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime tomorrowEnd = tomorrowStart.plusDays(1);
        
        List<Appointment> upcomingAppointments = appointmentRepository.findAll();
        for (Appointment appt : upcomingAppointments) {
            if (appt.getStatus() == AppointmentStatus.BOOKED) {
                if (appt.getAppointmentTime().isAfter(tomorrowStart) && appt.getAppointmentTime().isBefore(tomorrowEnd)) {
                    notificationService.sendAppointmentReminder(appt);
                }
            }
        }

        // 2. Medication Reminders
        List<Medication> allMedications = medicationRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        
        for (Medication med : allMedications) {
            // Check if medication is still active
            if (med.getEndDate() == null || med.getEndDate().isAfter(now)) {
                // In a real app, we would parse "frequency" (e.g. TWICE_DAILY) and schedule exactly at those times.
                // For this demo, we send a daily morning reminder for all active medications.
                notificationService.sendMedicationReminder(med);
            }
        }
        
        System.out.println("Finished daily reminder scheduler.");
    }
}
