package com.example.learners.controller;

import com.example.learners.entity.User;
import com.example.learners.service.StudentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SimpleAuthController {
    
    private final StudentService studentService;
    
    /**
     * Show simple login page
     */
    @GetMapping({"/", "/login"})
    public String showLoginPage(Model model) {
        model.addAttribute("pageTitle", "Simple Login - Learner Dashboard");
        return "simple-login";
    }
    
    /**
     * Simple login - look up user in database and set session
     */
    @PostMapping("/login")
    public String processLogin(@RequestParam("email") String email,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        
        log.info("Login attempt for email: {}", email);
        
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            log.warn("Invalid email format: {}", email);
            redirectAttributes.addFlashAttribute("error", "Please enter a valid email address.");
            return "redirect:/login";
        }
        
        try {
            // Look up user in database
            Optional<User> userOptional = studentService.getUserByEmail(email.trim());
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                
                // Set session attributes
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("userName", user.getFirstName() + " " + user.getLastName());
                session.setAttribute("userEmail", user.getEmail());
                
                log.info("Login successful for user: {} ({})", user.getEmail(), user.getUserId());
                return "redirect:/dashboard";
                
            } else {
                // User doesn't exist - create a new user
                log.info("User not found, creating new user for email: {}", email);
                
                String[] emailParts = email.split("@");
                String firstName = emailParts[0];
                String lastName = "User";
                
                User newUser = studentService.createUser(firstName, lastName, email.trim());
                
                // Set session attributes for new user
                session.setAttribute("userId", newUser.getUserId());
                session.setAttribute("userName", newUser.getFirstName() + " " + newUser.getLastName());
                session.setAttribute("userEmail", newUser.getEmail());
                
                log.info("New user created and logged in: {} ({})", newUser.getEmail(), newUser.getUserId());
                return "redirect:/dashboard";
            }
            
        } catch (Exception e) {
            log.error("Error during login for email: {}", email, e);
            redirectAttributes.addFlashAttribute("error", "Login failed. Please try again.");
            return "redirect:/login";
        }
    }
    
    /**
     * Logout - clear session and redirect to login
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        log.info("User logged out, session invalidated");
        return "redirect:/login";
    }
}
