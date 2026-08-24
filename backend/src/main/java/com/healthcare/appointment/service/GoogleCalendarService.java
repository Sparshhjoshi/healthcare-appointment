package com.healthcare.appointment.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.healthcare.appointment.entity.Appointment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

@Service
public class GoogleCalendarService {

    private final Calendar googleCalendar;

    @Autowired
    public GoogleCalendarService(@Autowired(required = false) Calendar googleCalendar) {
        this.googleCalendar = googleCalendar;
    }

    @Value("${google.calendar.id:primary}")
    private String calendarId;

    public Optional<String> createEvent(Appointment appointment) {
        if (googleCalendar == null) {
            return Optional.empty();
        }

        try {
            Event event = new Event()
                .setSummary("Appointment with Dr. " + appointment.getDoctor().getLastName())
                .setDescription("Medical appointment for " + appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName());

            Date startDate = Date.from(appointment.getAppointmentTime().atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(appointment.getAppointmentTime().plusHours(1).atZone(ZoneId.systemDefault()).toInstant());

            EventDateTime start = new EventDateTime().setDateTime(new DateTime(startDate));
            EventDateTime end = new EventDateTime().setDateTime(new DateTime(endDate));
            event.setStart(start);
            event.setEnd(end);

            EventAttendee[] attendees = new EventAttendee[] {
                new EventAttendee().setEmail(appointment.getPatient().getEmail() != null ? appointment.getPatient().getEmail() : "patient@test.local"),
                new EventAttendee().setEmail(appointment.getDoctor().getEmail() != null ? appointment.getDoctor().getEmail() : "doctor@test.local"),
            };
            event.setAttendees(Arrays.asList(attendees));

            // Use the injected calendar ID
            event = googleCalendar.events().insert(calendarId, event).execute();
            
            System.out.println("Google Calendar event created: " + event.getHtmlLink());
            return Optional.of(event.getId());

        } catch (Exception e) {
            System.err.println("Error creating Google Calendar event: " + e.getMessage());
            // Fail gracefully - don't throw, just return empty so the appointment still succeeds
            return Optional.empty();
        }
    }

    public void deleteEvent(String eventId) {
        if (googleCalendar == null || eventId == null || eventId.trim().isEmpty()) {
            return;
        }

        try {
            googleCalendar.events().delete(calendarId, eventId).execute();
            System.out.println("Google Calendar event deleted: " + eventId);
        } catch (Exception e) {
            System.err.println("Error deleting Google Calendar event: " + e.getMessage());
        }
    }
}
