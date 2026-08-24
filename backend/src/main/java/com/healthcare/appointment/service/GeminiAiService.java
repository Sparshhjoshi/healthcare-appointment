package com.healthcare.appointment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiAiService {

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public GeminiAiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generatePreVisitSummary(String symptoms) {
        String prompt = "Analyse these symptoms and return: urgency level (Low / Medium / High), chief complaint, and three suggested questions for the doctor. Symptoms: " + symptoms;
        return callGeminiApi(prompt);
    }

    public String generatePostVisitSummary(String notes) {
        String prompt = "Convert these clinical notes into a patient-friendly summary with medication schedule and follow-up steps: " + notes;
        return callGeminiApi(prompt);
    }

    private String callGeminiApi(String prompt) {
        if (apiKey == null || apiKey.equals("YOUR_API_KEY_HERE") || apiKey.trim().isEmpty()) {
            return "AI Summary is disabled because no API key was provided.";
        }

        String url = apiUrl + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Build the complex JSON request body required by Gemini
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            // Send POST request
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            
            // Extract the text from the complex JSON response
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
            
        } catch (org.springframework.web.client.RestClientException e) {
            System.err.println("Gemini API Network/Timeout Error: " + e.getMessage());
            return "AI Summary temporarily unavailable due to system load. Please proceed manually.";
        } catch (Exception e) {
            System.err.println("Gemini API Parsing/Unknown Error: " + e.getMessage());
            return "AI Summary temporarily unavailable. Please proceed manually.";
        }
    }
}
