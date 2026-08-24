package com.healthcare.appointment.repository;

import com.healthcare.appointment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository gives us built-in methods like save(), findById(), findAll(), deleteById()
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Spring Data JPA magically generates the SQL query for this method!
    Optional<User> findByEmail(String email);
}
