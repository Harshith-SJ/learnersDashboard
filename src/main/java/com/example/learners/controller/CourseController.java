package com.example.learners.controller;

import com.example.learners.entity.Course;
import com.example.learners.service.CourseService;
import com.example.learners.service.StudentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CourseController {
    
    private final CourseService courseService;
    private final StudentService studentService;
    
    /**
     * All Courses page - Display all available courses dynamically with category filter
     */
    @GetMapping("/courses")
    public String showAllCourses(@RequestParam(value = "category", required = false) String category,
                                HttpSession session, Model model) {
        
        Integer userId = (Integer) session.getAttribute("userId");
        String userName = (String) session.getAttribute("userName");
        
        if (userId == null || userName == null) {
            return "redirect:/login";
        }
        
        log.info("Loading courses page for user: {} with category filter: {}", userName, category);
        
        try {
            // Get courses based on category filter
            List<Course> allCourses;
            if (category != null && !category.isEmpty() && !category.equals("all")) {
                allCourses = courseService.getCoursesByCategory(category);
                log.info("Filtering courses by category: {}", category);
            } else {
                allCourses = courseService.getAllCourses();
                log.info("Loading all courses");
            }
            
            // Get all available categories for dropdown
            List<String> availableCategories = courseService.getAllCategories();
            
            // Get user's enrolled course IDs for efficient enrollment checking
            List<Integer> enrolledCourseIds = studentService.getUserEnrollments(userId)
                .stream()
                .map(enrollment -> enrollment.getCourse().getCourseId())
                .toList();
            
            // Mark enrolled courses
            allCourses.forEach(course -> {
                boolean isEnrolled = enrolledCourseIds.contains(course.getCourseId());
                if (isEnrolled) {
                    // You could add a transient field or handle this in template
                    log.debug("User {} is enrolled in course: {}", userId, course.getTitle());
                }
            });
            
            model.addAttribute("userName", userName);
            model.addAttribute("courses", allCourses);
            model.addAttribute("enrolledCourseIds", enrolledCourseIds);
            model.addAttribute("activePage", "courses"); // For navbar active state
            model.addAttribute("availableCategories", availableCategories); // For dropdown filter
            model.addAttribute("selectedCategory", category != null ? category : "all"); // Current filter
            
            log.info("Loaded {} courses for display", allCourses.size());
            return "courses";
            
        } catch (Exception e) {
            log.error("Error loading courses for user: {}", userId, e);
            model.addAttribute("error", "Error loading courses");
            return "error";
        }
    }
    
    /**
     * Enroll in a course
     */
    @PostMapping("/courses/{courseId}/enroll")
    public String enrollInCourse(@PathVariable("courseId") Integer courseId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to enroll in courses.");
            return "redirect:/login";
        }
        
        log.info("Enrollment request - User: {}, Course: {}", userId, courseId);
        
        try {
            // Check if course exists and is valid
            Optional<Course> courseOpt = courseService.getCourseById(courseId);
            if (courseOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Course not found.");
                return "redirect:/courses";
            }
            
            Course course = courseOpt.get();
            log.info("Course found: {} (Status: {})", course.getTitle(), course.getStatus());
            
            // Check if user can enroll
            boolean canEnroll = courseService.canStudentEnroll(userId, courseId);
            log.info("Can user {} enroll in course {}? {}", userId, courseId, canEnroll);
            
            if (!canEnroll) {
                // Check specific reasons
                boolean isAlreadyEnrolled = studentService.isEnrolledInCourse(userId, courseId);
                boolean isCourseAvailable = courseService.isCourseAvailableForEnrollment(courseId);
                
                if (isAlreadyEnrolled) {
                    redirectAttributes.addFlashAttribute("error", "You are already enrolled in this course.");
                } else if (!isCourseAvailable) {
                    redirectAttributes.addFlashAttribute("error", "This course is not currently available for enrollment.");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Cannot enroll in this course at this time.");
                }
                return "redirect:/courses";
            }
            
            // Attempt enrollment
            boolean success = studentService.enrollInCourse(userId, courseId);
            
            if (success) {
                redirectAttributes.addFlashAttribute("success", 
                    "Successfully enrolled in \"" + course.getTitle() + "\"! Check 'My Courses' to start learning.");
                log.info("Successfully enrolled user {} in course {}", userId, courseId);
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Enrollment failed. Please try again.");
                log.error("Enrollment failed for user {} in course {}", userId, courseId);
            }
            
        } catch (Exception e) {
            log.error("Error enrolling user {} in course {}", userId, courseId, e);
            redirectAttributes.addFlashAttribute("error", 
                "An error occurred during enrollment: " + e.getMessage());
        }
        
        return "redirect:/courses";
    }
}
