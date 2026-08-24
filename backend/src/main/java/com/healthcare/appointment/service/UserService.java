package com.healthcare.appointment.service;

import com.healthcare.appointment.dto.UserRegistrationDto;
import com.healthcare.appointment.entity.User;
import com.healthcare.appointment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User registerUser(UserRegistrationDto dto) {
        // 1. Check if email already exists
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already taken!");
        }

        // 2. Convert DTO to Entity
        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                // NOTE: We will hash this password in Phase 3 when we add Spring Security!
                .password(dto.getPassword()) 
                .role(dto.getRole())
                .build();

        // 3. Save to database using our Repository
        return userRepository.save(user);
    }
}
