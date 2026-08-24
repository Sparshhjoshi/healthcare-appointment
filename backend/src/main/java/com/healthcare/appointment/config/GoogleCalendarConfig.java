package com.healthcare.appointment.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;

@Configuration
public class GoogleCalendarConfig {

    @Value("${GOOGLE_CREDENTIALS_JSON:#{null}}")
    private String credentialsJson;

    @Bean
    public Calendar googleCalendarClient() {
        try {
            GoogleCredential credential = null;

            // 1. Try reading from environment variable first (for Render Production)
            if (credentialsJson != null && !credentialsJson.trim().isEmpty()) {
                credential = GoogleCredential.fromStream(new java.io.ByteArrayInputStream(credentialsJson.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                        .createScoped(Collections.singleton(CalendarScopes.CALENDAR));
            } 
            // 2. Fallback to local file (for Local Development)
            else {
                File credentialsFile = new File("credentials.json");
                if (credentialsFile.exists()) {
                    credential = GoogleCredential.fromStream(new FileInputStream(credentialsFile))
                            .createScoped(Collections.singleton(CalendarScopes.CALENDAR));
                }
            }

            if (credential == null) {
                System.out.println("WARNING: Google Calendar credentials not found (checked env var GOOGLE_CREDENTIALS_JSON and local file credentials.json). Google Calendar integration will be disabled.");
                return null;
            }

            return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), credential)
                    .setApplicationName("Healthcare Appointment Manager")
                    .build();

        } catch (Exception e) {
            System.err.println("Failed to initialize Google Calendar client: " + e.getMessage());
            return null;
        }
    }
}
