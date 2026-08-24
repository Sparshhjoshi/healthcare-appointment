package com.healthcare.appointment.service;

import com.healthcare.appointment.entity.Appointment;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.healthcare.appointment.entity.Medication;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.test.email:YOUR_TEST_EMAIL@gmail.com}")
    private String testEmail;

    public void sendBookingConfirmation(Appointment appointment) {
        String patientEmail = appointment.getPatient().getEmail();
        String doctorEmail = appointment.getDoctor().getEmail();
        
        // Fallback to testEmail if real emails are somehow missing
        if (patientEmail == null || patientEmail.trim().isEmpty()) patientEmail = testEmail;
        if (doctorEmail == null || doctorEmail.trim().isEmpty()) doctorEmail = testEmail;
        
        String doctorName = "Dr. " + appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName();
        String patientName = appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName();
        String dateStr = appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a"));

        try {
            // 1. Send Email to the PATIENT
            MimeMessage patientMsg = mailSender.createMimeMessage();
            MimeMessageHelper patientHelper = new MimeMessageHelper(patientMsg, true, "UTF-8");

            patientHelper.setTo(patientEmail);
            patientHelper.setSubject("Appointment Confirmed with " + doctorName);
            
            String patientHtml = "<h3>Your Appointment is Confirmed!</h3>"
                    + "<p>Dear " + appointment.getPatient().getFirstName() + ",</p>"
                    + "<p>You are scheduled to see <b>" + doctorName + "</b> on <b>" + dateStr + "</b>.</p>"
                    + "<p>We have attached a calendar invite (.ics) to this email.</p>"
                    + "<br><p>Thank you,<br>Healthcare Appointment Manager</p>";
            
            patientHelper.setText(patientHtml, true);
            
            String icsContent = generateIcsContent(appointment, false);
            patientHelper.addAttachment("appointment.ics", new ByteArrayResource(icsContent.getBytes(StandardCharsets.UTF_8)));
            mailSender.send(patientMsg);
            System.out.println("SUCCESS: Patient confirmation email sent.");

            // 2. Send Email to the DOCTOR
            MimeMessage doctorMsg = mailSender.createMimeMessage();
            MimeMessageHelper doctorHelper = new MimeMessageHelper(doctorMsg, true, "UTF-8");

            doctorHelper.setTo(doctorEmail);
            doctorHelper.setSubject("New Appointment Scheduled: " + patientName);
            
            String doctorHtml = "<h3>New Patient Appointment</h3>"
                    + "<p>Dear " + doctorName + ",</p>"
                    + "<p>A new appointment has been booked in your schedule.</p>"
                    + "<p><b>Patient:</b> " + patientName + "</p>"
                    + "<p><b>Time:</b> " + dateStr + "</p>"
                    + "<p>Please check your Doctor Portal to view the patient's AI-generated Chief Complaint before the visit.</p>"
                    + "<br><p>Thank you,<br>Healthcare Appointment Manager</p>";

            doctorHelper.setText(doctorHtml, true);
            doctorHelper.addAttachment("patient_appointment.ics", new ByteArrayResource(icsContent.getBytes(StandardCharsets.UTF_8)));
            mailSender.send(doctorMsg);
            System.out.println("SUCCESS: Doctor notification email sent.");

        } catch (MessagingException e) {
            System.err.println("FAILED to send email: " + e.getMessage());
        }
    }

    public void sendCancellationEmail(Appointment appointment) {
        String patientEmail = appointment.getPatient().getEmail();
        String doctorEmail = appointment.getDoctor().getEmail();
        
        if (patientEmail == null || patientEmail.trim().isEmpty()) patientEmail = testEmail;
        if (doctorEmail == null || doctorEmail.trim().isEmpty()) doctorEmail = testEmail;

        String doctorName = "Dr. " + appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName();
        String patientName = appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName();
        String dateStr = appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a"));
        String icsContent = generateIcsContent(appointment, true);

        try {
            // Patient Cancellation
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(patientEmail);
            helper.setSubject("Appointment CANCELLED - " + doctorName);
            
            String html = "<h3>Appointment Cancelled</h3>"
                    + "<p>Dear " + appointment.getPatient().getFirstName() + ",</p>"
                    + "<p>Your appointment with <b>" + doctorName + "</b> on <b>" + dateStr + "</b> has been cancelled.</p>"
                    + "<p>Please log in to your dashboard to reschedule.</p>"
                    + "<br><p>Thank you,<br>Healthcare Appointment Manager</p>";
            
            helper.setText(html, true);
            helper.addAttachment("cancellation.ics", new ByteArrayResource(icsContent.getBytes(StandardCharsets.UTF_8)));
            mailSender.send(message);

            // Doctor Cancellation
            MimeMessage doctorMsg = mailSender.createMimeMessage();
            MimeMessageHelper doctorHelper = new MimeMessageHelper(doctorMsg, true, "UTF-8");

            doctorHelper.setTo(doctorEmail);
            doctorHelper.setSubject("Appointment CANCELLED - " + patientName);
            
            String doctorHtml = "<h3>Appointment Cancelled</h3>"
                    + "<p>Dear " + doctorName + ",</p>"
                    + "<p>The appointment with <b>" + patientName + "</b> on <b>" + dateStr + "</b> has been cancelled.</p>"
                    + "<br><p>Thank you,<br>Healthcare Appointment Manager</p>";
            
            doctorHelper.setText(doctorHtml, true);
            doctorHelper.addAttachment("cancellation.ics", new ByteArrayResource(icsContent.getBytes(StandardCharsets.UTF_8)));
            mailSender.send(doctorMsg);

            System.out.println("SUCCESS: Cancellation emails sent.");
        } catch (MessagingException e) {
            System.err.println("FAILED to send cancellation email: " + e.getMessage());
        }
    }

    public void sendAppointmentReminder(Appointment appointment) {
        String patientEmail = appointment.getPatient().getEmail();
        String doctorEmail = appointment.getDoctor().getEmail();

        if (patientEmail == null || patientEmail.trim().isEmpty()) patientEmail = testEmail;
        if (doctorEmail == null || doctorEmail.trim().isEmpty()) doctorEmail = testEmail;

        String doctorName = "Dr. " + appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName();
        String patientName = appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName();
        String dateStr = appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a"));

        try {
            // Patient Reminder
            MimeMessage patientMsg = mailSender.createMimeMessage();
            MimeMessageHelper patientHelper = new MimeMessageHelper(patientMsg, true, "UTF-8");

            patientHelper.setTo(patientEmail);
            patientHelper.setSubject("REMINDER: Appointment Tomorrow with " + doctorName);
            
            String patientHtml = "<h3>Appointment Reminder</h3>"
                    + "<p>Dear " + appointment.getPatient().getFirstName() + ",</p>"
                    + "<p>This is a reminder that you have an appointment with <b>" + doctorName + "</b> tomorrow, <b>" + dateStr + "</b>.</p>"
                    + "<br><p>Thank you,<br>Healthcare Appointment Manager</p>";
            
            patientHelper.setText(patientHtml, true);
            mailSender.send(patientMsg);

            // Doctor Reminder
            MimeMessage doctorMsg = mailSender.createMimeMessage();
            MimeMessageHelper doctorHelper = new MimeMessageHelper(doctorMsg, true, "UTF-8");

            doctorHelper.setTo(doctorEmail);
            doctorHelper.setSubject("REMINDER: Appointment Tomorrow with " + patientName);
            
            String doctorHtml = "<h3>Appointment Reminder</h3>"
                    + "<p>Dear " + doctorName + ",</p>"
                    + "<p>This is a reminder that you have an appointment with <b>" + patientName + "</b> tomorrow, <b>" + dateStr + "</b>.</p>"
                    + "<br><p>Thank you,<br>Healthcare Appointment Manager</p>";
            
            doctorHelper.setText(doctorHtml, true);
            mailSender.send(doctorMsg);

            System.out.println("SUCCESS: Reminder emails sent.");
        } catch (MessagingException e) {
            System.err.println("FAILED to send reminder email: " + e.getMessage());
        }
    }

    public void sendMedicationReminder(Medication medication) {
        String patientEmail = medication.getPatient().getEmail();
        if (patientEmail == null || patientEmail.trim().isEmpty()) patientEmail = testEmail;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(patientEmail);
            helper.setSubject("Medication Reminder: " + medication.getName());
            
            String html = "<h3>Medication Reminder</h3>"
                    + "<p>Dear " + medication.getPatient().getFirstName() + ",</p>"
                    + "<p>This is your reminder to take your medication: <b>" + medication.getName() + "</b>.</p>"
                    + "<p>Frequency: " + medication.getFrequency() + "</p>"
                    + "<br><p>Please take it as prescribed by your doctor.</p>"
                    + "<br><p>Thank you,<br>Healthcare Appointment Manager</p>";
            
            helper.setText(html, true);
            mailSender.send(message);
            System.out.println("SUCCESS: Medication reminder email sent for " + medication.getName());
        } catch (MessagingException e) {
            System.err.println("FAILED to send medication reminder: " + e.getMessage());
        }
    }

    private String generateIcsContent(Appointment appointment, boolean isCancel) {
        // Convert LocalDateTime to UTC for the ICS format (yyyyMMdd'T'HHmmss'Z')
        ZonedDateTime startTime = appointment.getAppointmentTime().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime endTime = startTime.plusHours(1); // Assume 1 hour appointment

        DateTimeFormatter icsFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        
        String dtStart = startTime.format(icsFormatter);
        String dtEnd = endTime.format(icsFormatter);
        String dtStamp = ZonedDateTime.now(ZoneId.of("UTC")).format(icsFormatter);
        String uid = "apt-" + appointment.getId() + "@healthcare.local";
        String doctorName = "Dr. " + appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName();

        return "BEGIN:VCALENDAR\r\n" +
               "VERSION:2.0\r\n" +
               "PRODID:-//Healthcare Appointment Manager//EN\r\n" +
               "METHOD:" + (isCancel ? "CANCEL" : "REQUEST") + "\r\n" +
               "BEGIN:VEVENT\r\n" +
               "UID:" + uid + "\r\n" +
               "DTSTAMP:" + dtStamp + "\r\n" +
               "DTSTART:" + dtStart + "\r\n" +
               "DTEND:" + dtEnd + "\r\n" +
               "SUMMARY:Appointment with " + doctorName + "\r\n" +
               "DESCRIPTION:Medical appointment for " + appointment.getPatient().getFirstName() + "\r\n" +
               "STATUS:" + (isCancel ? "CANCELLED" : "CONFIRMED") + "\r\n" +
               "END:VEVENT\r\n" +
               "END:VCALENDAR\r\n";
    }
}
