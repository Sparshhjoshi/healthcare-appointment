package com.healthcare.appointment.service;

import com.healthcare.appointment.entity.Appointment;
import com.healthcare.appointment.entity.Medication;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class NotificationService {

    @Value("${sendgrid.api.key:}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email:noreply@healthcare-appointment.com}")
    private String fromEmailAddress;

    @Value("${app.test.email:YOUR_TEST_EMAIL@gmail.com}")
    private String testEmail;

    private void sendSendGridEmail(String toEmail, String subject, String htmlContent, String attachmentFilename, String attachmentContent) throws IOException {
        if (sendGridApiKey == null || sendGridApiKey.isEmpty()) {
            System.err.println("SendGrid API Key is missing. Skipping email.");
            return;
        }

        Email from = new Email(fromEmailAddress);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        // Always BCC testEmail
        Personalization personalization = mail.getPersonalization().get(0);
        if (testEmail != null && !testEmail.trim().isEmpty() && !testEmail.equals(toEmail)) {
            personalization.addBcc(new Email(testEmail));
        }

        if (attachmentFilename != null && attachmentContent != null) {
            Attachments attachments = new Attachments();
            attachments.setContent(Base64.getEncoder().encodeToString(attachmentContent.getBytes(StandardCharsets.UTF_8)));
            attachments.setType("text/calendar");
            attachments.setFilename(attachmentFilename);
            attachments.setDisposition("attachment");
            mail.addAttachments(attachments);
        }

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            System.out.println("SUCCESS: Email sent via SendGrid to " + toEmail);
        } else {
            System.err.println("FAILED to send email via SendGrid. Status Code: " + response.getStatusCode() + " Body: " + response.getBody());
        }
    }

    @Async
    public void sendBookingConfirmation(Appointment appointment) {
        String patientEmail = appointment.getPatient().getEmail();
        String doctorEmail = appointment.getDoctor().getEmail();
        
        if (patientEmail == null || patientEmail.trim().isEmpty()) patientEmail = testEmail;
        if (doctorEmail == null || doctorEmail.trim().isEmpty()) doctorEmail = testEmail;
        
        String doctorName = "Dr. " + appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName();
        String patientName = appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName();
        String dateStr = appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a"));
        String icsContent = generateIcsContent(appointment, false);

        try {
            // 1. Send Email to the PATIENT
            String patientHtml = "<h3>Your Appointment is Confirmed!</h3>"
                    + "<p>Dear " + appointment.getPatient().getFirstName() + ",</p>"
                    + "<p>You are scheduled to see <b>" + doctorName + "</b> on <b>" + dateStr + "</b>.</p>"
                    + "<p>We have attached a calendar invite (.ics) to this email.</p>"
                    + "<br><p>Thank you,<br>Healthcare Appointment Manager</p>";
            
            sendSendGridEmail(patientEmail, "Appointment Confirmed with " + doctorName, patientHtml, "appointment.ics", icsContent);

            // 2. Send Email to the DOCTOR
            String doctorHtml = "<h3>New Patient Appointment</h3>"
                    + "<p>Dear " + doctorName + ",</p>"
                    + "<p>A new appointment has been booked in your schedule.</p>"
                    + "<p><b>Patient:</b> " + patientName + "</p>"
                    + "<p><b>Time:</b> " + dateStr + "</p>"
                    + "<p>Please check your Doctor Portal to view the patient's AI-generated Chief Complaint before the visit.</p>"
                    + "<br><p>Thank you,<br>Healthcare Appointment Manager</p>";

            sendSendGridEmail(doctorEmail, "New Appointment Scheduled: " + patientName, doctorHtml, "patient_appointment.ics", icsContent);

        } catch (Exception e) {
            System.err.println("FAILED to send email via SendGrid: " + e.getMessage());
        }
    }

    @Async
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
            String html = "<h3>Appointment Cancelled</h3>"
                    + "<p>Dear " + appointment.getPatient().getFirstName() + ",</p>"
                    + "<p>Your appointment with <b>" + doctorName + "</b> on <b>" + dateStr + "</b> has been cancelled.</p>"
                    + "<p>Please log in to your dashboard to reschedule.</p>"
                    + "<br><p>Thank you,<br>Healthcare Appointment Manager</p>";
            
            sendSendGridEmail(patientEmail, "Appointment CANCELLED - " + doctorName, html, "cancellation.ics", icsContent);

            // Doctor Cancellation
            String doctorHtml = "<h3>Appointment Cancelled</h3>"
                    + "<p>Dear " + doctorName + ",</p>"
                    + "<p>The appointment with <b>" + patientName + "</b> on <b>" + dateStr + "</b> has been cancelled.</p>"
                    + "<br><p>Thank you,<br>Healthcare Appointment Manager</p>";
            
            sendSendGridEmail(doctorEmail, "Appointment CANCELLED - " + patientName, doctorHtml, "cancellation.ics", icsContent);

        } catch (Exception e) {
            System.err.println("FAILED to send cancellation email via SendGrid: " + e.getMessage());
        }
    }

    @Async
    public void sendAppointmentReminder(Appointment appointment) {
        String patientEmail = appointment.getPatient().getEmail();
        String doctorEmail = appointment.getDoctor().getEmail();

        if (patientEmail == null || patientEmail.trim().isEmpty()) patientEmail = testEmail;
        if (doctorEmail == null || doctorEmail.trim().isEmpty()) doctorEmail = testEmail;

        String doctorName = "Dr. " + appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName();
        String patientName = appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName();
        String dateStr = appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a"));

        try {
            String patientHtml = "<h3>Appointment Reminder</h3>"
                    + "<p>Dear " + appointment.getPatient().getFirstName() + ",</p>"
                    + "<p>This is a reminder that you have an appointment with <b>" + doctorName + "</b> tomorrow, <b>" + dateStr + "</b>.</p>"
                    + "<br><p>Thank you,<br>Healthcare Appointment Manager</p>";
            
            sendSendGridEmail(patientEmail, "REMINDER: Appointment Tomorrow with " + doctorName, patientHtml, null, null);

            String doctorHtml = "<h3>Appointment Reminder</h3>"
                    + "<p>Dear " + doctorName + ",</p>"
                    + "<p>This is a reminder that you have an appointment with <b>" + patientName + "</b> tomorrow, <b>" + dateStr + "</b>.</p>"
                    + "<br><p>Thank you,<br>Healthcare Appointment Manager</p>";
            
            sendSendGridEmail(doctorEmail, "REMINDER: Appointment Tomorrow with " + patientName, doctorHtml, null, null);

        } catch (Exception e) {
            System.err.println("FAILED to send reminder email via SendGrid: " + e.getMessage());
        }
    }

    @Async
    public void sendMedicationReminder(Medication medication) {
        String patientEmail = medication.getPatient().getEmail();
        if (patientEmail == null || patientEmail.trim().isEmpty()) patientEmail = testEmail;

        try {
            String html = "<h3>Medication Reminder</h3>"
                    + "<p>Dear " + medication.getPatient().getFirstName() + ",</p>"
                    + "<p>This is your reminder to take your medication: <b>" + medication.getName() + "</b>.</p>"
                    + "<p>Frequency: " + medication.getFrequency() + "</p>"
                    + "<br><p>Please take it as prescribed by your doctor.</p>"
                    + "<br><p>Thank you,<br>Healthcare Appointment Manager</p>";
            
            sendSendGridEmail(patientEmail, "Medication Reminder: " + medication.getName(), html, null, null);
        } catch (Exception e) {
            System.err.println("FAILED to send medication reminder via SendGrid: " + e.getMessage());
        }
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
