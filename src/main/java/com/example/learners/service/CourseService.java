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
    public List<String> getAllCategories() {
        return courseRepository.findAll()
            .stream()
            .map(Course::getCategory)
            .distinct()
            .toList();
    }

    public Optional<Course> getCourseById(Integer courseId) {
        return courseRepository.findById(courseId);
    }
    private final CourseRepository courseRepository;
    private final CourseMaterialRepository courseMaterialRepository;
    private final EnrollmentRepository enrollmentRepository;

    public List<Course> getAllCourses() {
        log.info("Fetching all courses");
        return courseRepository.findAll();
    }

    public List<Course> getAllActiveCourses() {
        log.info("Fetching all active courses");
        return courseRepository.findByStatusOrderByCreatedAtDesc(Course.CourseStatus.active);
    }

    public Page<Course> getActiveCoursesWithPagination(int page, int size) {
        log.info("Fetching active courses with pagination: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return courseRepository.findByStatusOrderByCreatedAtDesc(Course.CourseStatus.active, pageable);
    }

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

    public List<Course> getActiveCoursesByCategory(String category) {
        log.info("Fetching active courses by category: {}", category);
        if (category == null || category.trim().isEmpty()) {
            return getAllActiveCourses();
        }
        return courseRepository.findByStatusAndCategoryOrderByCreatedAtDesc(
            Course.CourseStatus.active, category);
    }

    public List<Course> getCoursesByLevel(Course.CourseLevel level) {
        log.info("Fetching courses by level: {}", level);
        if (level == null) {
            return getAllActiveCourses();
        }
        return courseRepository.findByStatusAndLevelOrderByCreatedAtDesc(
            Course.CourseStatus.active, level);
    }

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

    public List<Course> searchCourses(String keyword) {
        log.info("Searching courses with keyword: {}", keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActiveCourses();
        }
        return courseRepository.searchCoursesByKeyword(Course.CourseStatus.active, keyword.trim());
    }

    public boolean isCourseAvailableForEnrollment(Integer courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isPresent()) {
            Course c = courseOpt.get();
            boolean isAvailable = c.getStatus() == Course.CourseStatus.active;
            log.info("Course {} status: {}, available for enrollment: {}", courseId, c.getStatus(), isAvailable);
            return isAvailable;
        }
        log.warn("Course {} not found", courseId);
        return false;
    }

    public boolean canStudentEnroll(Integer studentId, Integer courseId) {
        log.info("Checking if student {} can enroll in course {}", studentId, courseId);
        try {
            if (!isCourseAvailableForEnrollment(courseId)) {
                log.info("Course {} is not available for enrollment", courseId);
                return false;
            }
            boolean alreadyEnrolled = enrollmentRepository.existsByStudentUserIdAndCourseId(studentId, courseId);
            log.info("Student {} already enrolled in course {}: {}", studentId, courseId, alreadyEnrolled);
            return !alreadyEnrolled;
        } catch (Exception e) {
            log.error("Error checking enrollment eligibility for student {} and course {}: {}", 
                     studentId, courseId, e.getMessage());
            return false;
        }
    }

    public long getTotalActiveCourses() {
        return courseRepository.countByStatus(Course.CourseStatus.active);
    }
}
