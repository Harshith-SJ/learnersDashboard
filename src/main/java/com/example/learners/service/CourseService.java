package com.example.learners.service;

import com.example.learners.entity.Course;
import com.example.learners.entity.CourseMaterial;
import com.example.learners.repository.CourseRepository;
import com.example.learners.repository.CourseMaterialRepository;
import com.example.learners.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CourseService {
    
    private final CourseRepository courseRepository;
    private final CourseMaterialRepository courseMaterialRepository;
    private final EnrollmentRepository enrollmentRepository;
    
    /**
     * Get all courses for browsing (including active, upcoming, archived)
     */
    public List<Course> getAllCourses() {
        log.info("Fetching all courses");
        return courseRepository.findAll();
    }
    
    /**
     * Get all active courses for browsing
     */
    public List<Course> getAllActiveCourses() {
        log.info("Fetching all active courses");
        return courseRepository.findByStatusOrderByCreatedAtDesc(Course.CourseStatus.active);
    }
    
    /**
     * Get active courses with pagination
     */
    public Page<Course> getActiveCoursesWithPagination(int page, int size) {
        log.info("Fetching active courses with pagination: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return courseRepository.findByStatusOrderByCreatedAtDesc(Course.CourseStatus.active, pageable);
    }
    
    /**
     * Filter courses by category (all statuses)
     */
    public List<Course> getCoursesByCategory(String category) {
        log.info("Fetching courses by category: {}", category);
        if (category == null || category.trim().isEmpty()) {
            return getAllCourses();
        }
        return courseRepository.findAll()
                .stream()
                .filter(course -> category.equals(course.getCategory()))
                .sorted((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()))
                .toList();
    }
    
    /**
     * Filter active courses by category
     */
    public List<Course> getActiveCoursesByCategory(String category) {
        log.info("Fetching active courses by category: {}", category);
        if (category == null || category.trim().isEmpty()) {
            return getAllActiveCourses();
        }
        return courseRepository.findByStatusAndCategoryOrderByCreatedAtDesc(
            Course.CourseStatus.active, category);
    }
    
    /**
     * Filter courses by level
     */
    public List<Course> getCoursesByLevel(Course.CourseLevel level) {
        log.info("Fetching courses by level: {}", level);
        if (level == null) {
            return getAllActiveCourses();
        }
        return courseRepository.findByStatusAndLevelOrderByCreatedAtDesc(
            Course.CourseStatus.active, level);
    }
    
    /**
     * Filter courses by category and level
     */
    public List<Course> getCoursesByCategoryAndLevel(String category, Course.CourseLevel level) {
        log.info("Fetching courses by category: {} and level: {}", category, level);
        
        if ((category == null || category.trim().isEmpty()) && level == null) {
            return getAllActiveCourses();
        } else if (category == null || category.trim().isEmpty()) {
            return getCoursesByLevel(level);
        } else if (level == null) {
            return getCoursesByCategory(category);
        } else {
            return courseRepository.findByStatusAndCategoryAndLevelOrderByCreatedAtDesc(
                Course.CourseStatus.active, category, level);
        }
    }
    
    /**
     * Search courses by keyword
     */
    public List<Course> searchCourses(String keyword) {
        log.info("Searching courses with keyword: {}", keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActiveCourses();
        }
        return courseRepository.searchCoursesByKeyword(Course.CourseStatus.active, keyword.trim());
    }
    
    /**
     * Get distinct categories for filter dropdown (from active courses)
     */
    public List<String> getActiveCategories() {
        log.info("Fetching active course categories");
        return courseRepository.findDistinctCategoriesByStatus(Course.CourseStatus.active);
    }
    
    /**
     * Get all distinct categories for filter dropdown (from all courses)
     */
    public List<String> getAllCategories() {
        log.info("Fetching all course categories");
        return courseRepository.findAll()
                .stream()
                .map(Course::getCategory)
                .filter(category -> category != null && !category.trim().isEmpty())
                .distinct()
                .sorted()
                .toList();
    }
    
    /**
     * Get course by ID
     */
    public Optional<Course> getCourseById(Integer courseId) {
        log.info("Fetching course by ID: {}", courseId);
        return courseRepository.findById(courseId);
    }
    
    /**
     * Get course materials for a specific course
     */
    public List<CourseMaterial> getCourseMaterials(Integer courseId) {
        log.info("Fetching course materials for course: {}", courseId);
        return courseMaterialRepository.findByCourse_CourseIdOrderByCreatedAtDesc(courseId);
    }
    
    /**
     * Check if course is available for enrollment
     */
    public boolean isCourseAvailableForEnrollment(Integer courseId) {
        log.info("Checking if course {} is available for enrollment", courseId);
        try {
            Optional<Course> course = courseRepository.findById(courseId);
            if (course.isPresent()) {
                Course c = course.get();
                // Allow enrollment for active and upcoming courses
                boolean isAvailable = (c.getStatus() == Course.CourseStatus.active || 
                                     c.getStatus() == Course.CourseStatus.upcoming);
                log.info("Course {} status: {}, available for enrollment: {}", courseId, c.getStatus(), isAvailable);
                return isAvailable;
            }
            log.warn("Course {} not found", courseId);
            return false;
        } catch (Exception e) {
            log.error("Error checking course availability for course {}: {}", courseId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if student can enroll in course (not already enrolled + course available)
     */
    public boolean canStudentEnroll(Integer studentId, Integer courseId) {
        log.info("Checking if student {} can enroll in course {}", studentId, courseId);
        
        try {
            // Check if course is available
            if (!isCourseAvailableForEnrollment(courseId)) {
                log.info("Course {} is not available for enrollment", courseId);
                return false;
            }
            
            // Check if already enrolled
            boolean alreadyEnrolled = enrollmentRepository.existsByStudentUserIdAndCourseId(studentId, courseId);
            log.info("Student {} already enrolled in course {}: {}", studentId, courseId, alreadyEnrolled);
            
            return !alreadyEnrolled;
        } catch (Exception e) {
            log.error("Error checking enrollment eligibility for student {} and course {}: {}", 
                     studentId, courseId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Get total active courses count
     */
    public long getTotalActiveCourses() {
        return courseRepository.countByStatus(Course.CourseStatus.active);
    }
}
