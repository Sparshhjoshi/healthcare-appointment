package com.healthcare.appointment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // RestTemplate allows our Spring Boot app to make external HTTP requests (like calling Google's API)
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
