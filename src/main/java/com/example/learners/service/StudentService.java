package com.example.learners.service;

import com.example.learners.entity.User;
import com.example.learners.entity.Course;
import com.example.learners.entity.Enrollment;
import com.example.learners.repository.UserRepository;
import com.example.learners.repository.CourseRepository;
import com.example.learners.repository.EnrollmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class StudentService {
    /**
     * Internal method to create an enrollment
     */
    private Enrollment createEnrollment(Integer userId, Integer courseId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Course> course = courseRepository.findById(courseId);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        if (course.isEmpty()) {
            throw new RuntimeException("Course not found with ID: " + courseId);
        }
        // Check if already enrolled
        Optional<Enrollment> existingEnrollment = enrollmentRepository
            .findByStudentUserIdAndCourseId(userId, courseId);
        if (existingEnrollment.isPresent()) {
            throw new RuntimeException("User is already enrolled in this course");
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(user.get());
        enrollment.setCourse(course.get());
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setStatus(Enrollment.EnrollmentStatus.assigned);
        return enrollmentRepository.save(enrollment);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    /**
     * Get user by email (for simple authentication)
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Create a new user (simple registration)
     */
    public User createUser(String firstName, String lastName, String email) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPasswordHash("temp"); // Simplified - no real password hashing
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Get all available courses
     */
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    /**
     * Get user's enrollments
     */
    public List<Enrollment> getUserEnrollments(Integer userId) {
        return enrollmentRepository.findStudentEnrollments(userId, null);
    }

    /**
     * Enroll user in a course
     */
    public boolean enrollInCourse(Integer userId, Integer courseId) {
        try {
            log.info("Attempting to enroll user {} in course {}", userId, courseId);
            createEnrollment(userId, courseId);
            log.info("Successfully enrolled user {} in course {}", userId, courseId);
            return true;
        } catch (Exception e) {
            log.error("Failed to enroll user {} in course {}: {}", userId, courseId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Create an enrollment (internal method)
     */

    /**
     * Get course by ID
     */

    /**
     * Check if user exists by email
     */
    
    /**
     * Check if user is enrolled in a course
     */
    public boolean isEnrolledInCourse(Integer userId, Integer courseId) {
        if (userId == null || courseId == null) {
            return false;
        }
        return enrollmentRepository.findByStudentUserIdAndCourseId(userId, courseId).isPresent();
    }
}
