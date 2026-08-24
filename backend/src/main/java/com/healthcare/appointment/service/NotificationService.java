package com.healthcare.appointment.service;

import com.healthcare.appointment.entity.Appointment;
import com.healthcare.appointment.entity.Medication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class NotificationService {

    @Value("${resend.api.key:}")
    private String resendApiKey;

    @Value("${resend.from.email:onboarding@resend.dev}")
    private String fromEmailAddress;

    @Value("${app.test.email:YOUR_TEST_EMAIL@gmail.com}")
    private String testEmail;

    private final RestTemplate restTemplate;

    public NotificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private void sendResendEmail(String toEmail, String subject, String htmlContent, String attachmentFilename, String attachmentContent) {
        if (resendApiKey == null || resendApiKey.isEmpty()) {
            System.err.println("Resend API Key is missing. Skipping email.");
            return;
        }

        try {
            String url = "https://api.resend.com/emails";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resendApiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("from", "Healthcare App <" + fromEmailAddress + ">");
            
            List<String> toEmails = new ArrayList<>();
            toEmails.add(toEmail);
            body.put("to", toEmails);

            if (testEmail != null && !testEmail.trim().isEmpty() && !testEmail.equals(toEmail)) {
                body.put("bcc", Collections.singletonList(testEmail));
            }

            body.put("subject", subject);
            body.put("html", htmlContent);

            if (attachmentFilename != null && attachmentContent != null) {
                Map<String, String> attachment = new HashMap<>();
                attachment.put("filename", attachmentFilename);
                attachment.put("content", Base64.getEncoder().encodeToString(attachmentContent.getBytes(StandardCharsets.UTF_8)));
                body.put("attachments", Collections.singletonList(attachment));
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("SUCCESS: Email sent via Resend to " + toEmail);
            } else {
                System.err.println("FAILED to send email via Resend. Status Code: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("FAILED to send email via Resend API: " + e.getMessage());
        }
    }

    @Async
    public void sendBookingConfirmation(Appointment appointment) {
        String patientEmail = appointment.getPatient().getEmail();
        String doctorEmail = appointment.getDoctor().getEmail();
        
        // FOR RESEND FREE TIER: Force all emails to route to your verified email
        patientEmail = testEmail;
        doctorEmail = testEmail;
        
        String doctorName = "Dr. " + appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName();
        String patientName = appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName();
        String dateStr = appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a"));
        String icsContent = generateIcsContent(appointment, false);

        // 1. Send Email to the PATIENT
        String patientHtml = "<h3>Your Appointment is Confirmed!</h3>"
                + "<p>Dear " + appointment.getPatient().getFirstName() + ",</p>"
                + "<p>You are scheduled to see <b>" + doctorName + "</b> on <b>" + dateStr + "</b>.</p>"
                + "<p>We have attached a calendar invite (.ics) to this email.</p>"
                + "<br><p>Thank you,<br>Healthcare Appointment Manager</p>";
        
        sendResendEmail(patientEmail, "Appointment Confirmed with " + doctorName, patientHtml, "appointment.ics", icsContent);

        // 2. Send Email to the DOCTOR
        String doctorHtml = "<h3>New Patient Appointment</h3>"
                + "<p>Dear " + doctorName + ",</p>"
                + "<p>A new appointment has been booked in your schedule.</p>"
                + "<p><b>Patient:</b> " + patientName + "</p>"
                + "<p><b>Time:</b> " + dateStr + "</p>"
                + "<p>Please check your Doctor Portal to view the patient's AI-generated Chief Complaint before the visit.</p>"
                + "<br><p>Thank you,<br>Healthcare Appointment Manager</p>";

        sendResendEmail(doctorEmail, "New Appointment Scheduled: " + patientName, doctorHtml, "patient_appointment.ics", icsContent);
    }

    @Async
    public void sendCancellationEmail(Appointment appointment) {
        String patientEmail = appointment.getPatient().getEmail();
        String doctorEmail = appointment.getDoctor().getEmail();
        
        // FOR RESEND FREE TIER: Force all emails to route to your verified email
        patientEmail = testEmail;
        doctorEmail = testEmail;

        String doctorName = "Dr. " + appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName();
        String patientName = appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName();
        String dateStr = appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a"));
        String icsContent = generateIcsContent(appointment, true);

        // Patient Cancellation
        String html = "<h3>Appointment Cancelled</h3>"
                + "<p>Dear " + appointment.getPatient().getFirstName() + ",</p>"
                + "<p>Your appointment with <b>" + doctorName + "</b> on <b>" + dateStr + "</b> has been cancelled.</p>"
                + "<p>Please log in to your dashboard to reschedule.</p>"
                + "<br><p>Thank you,<br>Healthcare Appointment Manager</p>";
        
        sendResendEmail(patientEmail, "Appointment CANCELLED - " + doctorName, html, "cancellation.ics", icsContent);

        // Doctor Cancellation
        String doctorHtml = "<h3>Appointment Cancelled</h3>"
                + "<p>Dear " + doctorName + ",</p>"
                + "<p>The appointment with <b>" + patientName + "</b> on <b>" + dateStr + "</b> has been cancelled.</p>"
                + "<br><p>Thank you,<br>Healthcare Appointment Manager</p>";
        
        sendResendEmail(doctorEmail, "Appointment CANCELLED - " + patientName, doctorHtml, "cancellation.ics", icsContent);
    }

    @Async
    public void sendAppointmentReminder(Appointment appointment) {
        String patientEmail = appointment.getPatient().getEmail();
        String doctorEmail = appointment.getDoctor().getEmail();

        // FOR RESEND FREE TIER: Force all emails to route to your verified email
        patientEmail = testEmail;
        doctorEmail = testEmail;

        String doctorName = "Dr. " + appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName();
        String patientName = appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName();
        String dateStr = appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a"));

        String patientHtml = "<h3>Appointment Reminder</h3>"
                + "<p>Dear " + appointment.getPatient().getFirstName() + ",</p>"
                + "<p>This is a reminder that you have an appointment with <b>" + doctorName + "</b> tomorrow, <b>" + dateStr + "</b>.</p>"
                + "<br><p>Thank you,<br>Healthcare Appointment Manager</p>";
        
        sendResendEmail(patientEmail, "REMINDER: Appointment Tomorrow with " + doctorName, patientHtml, null, null);

        String doctorHtml = "<h3>Appointment Reminder</h3>"
                + "<p>Dear " + doctorName + ",</p>"
                + "<p>This is a reminder that you have an appointment with <b>" + patientName + "</b> tomorrow, <b>" + dateStr + "</b>.</p>"
                + "<br><p>Thank you,<br>Healthcare Appointment Manager</p>";
        
        sendResendEmail(doctorEmail, "REMINDER: Appointment Tomorrow with " + patientName, doctorHtml, null, null);
    }

    @Async
    public void sendMedicationReminder(Medication medication) {
        String patientEmail = medication.getPatient().getEmail();
        
        // FOR RESEND FREE TIER: Force all emails to route to your verified email
        patientEmail = testEmail;

        String html = "<h3>Medication Reminder</h3>"
                + "<p>Dear " + medication.getPatient().getFirstName() + ",</p>"
                + "<p>This is your reminder to take your medication: <b>" + medication.getName() + "</b>.</p>"
                + "<p>Frequency: " + medication.getFrequency() + "</p>"
                + "<br><p>Please take it as prescribed by your doctor.</p>"
                + "<br><p>Thank you,<br>Healthcare Appointment Manager</p>";
        
        sendResendEmail(patientEmail, "Medication Reminder: " + medication.getName(), html, null, null);
    }

    private String generateIcsContent(Appointment appointment, boolean isCancel) {
        ZonedDateTime startTime = appointment.getAppointmentTime().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime endTime = startTime.plusHours(1);

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
