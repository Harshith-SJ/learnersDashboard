package com.example.learners.repository;

import com.example.learners.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    // Find user by email for simple login
    Optional<User> findByEmail(String email);
    
    // Check if email exists (for validation)
    boolean existsByEmail(String email);
}
