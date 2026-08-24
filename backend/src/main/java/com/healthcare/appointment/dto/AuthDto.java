package com.healthcare.appointment.dto;

import com.healthcare.appointment.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class AuthDto {

    @Data
    public static class LoginRequest {
        @NotBlank
        @Email
        private String email;
        
        @NotBlank
        private String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank
        private String firstName;
        
        @NotBlank
        private String lastName;
        
        @NotBlank
        @Email
        private String email;
        
        @NotBlank
        private String password;
        
        private Role role = Role.PATIENT; // Default to PATIENT
    }

    @Data
    public static class AuthResponse {
        private String token;
        private com.healthcare.appointment.entity.User user;

        public AuthResponse(String token, com.healthcare.appointment.entity.User user) {
            this.token = token;
            this.user = user;
        }
    }
}
