package com.example.learners.controller;

import com.example.learners.entity.Course;
import com.example.learners.entity.Enrollment;
import com.example.learners.entity.Certification;
import com.example.learners.service.CourseService;
import com.example.learners.service.StudentService;
import com.example.learners.service.DashboardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardController {
    
    private final CourseService courseService;
    private final StudentService studentService;
    private final DashboardService dashboardService;
    
    /**
     * Main Dashboard - Overview of student's learning journey
     */
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        
        Integer userId = (Integer) session.getAttribute("userId");
        String userName = (String) session.getAttribute("userName");
        
        if (userId == null || userName == null) {
            log.warn("User not found in session, redirecting to login");
            return "redirect:/login";
        }
        
        log.info("Loading dashboard for user: {} ({})", userName, userId);
        
        try {
            // Get dashboard statistics
            Map<String, Object> stats = dashboardService.getDashboardStats(userId);
            
            // Get recent enrollments (last 5)
            List<Enrollment> recentEnrollments = dashboardService.getRecentEnrollments(userId, 5);
            
            // Get recent certifications (top 3)
            List<Certification> topCertifications = dashboardService.getRecentCertifications(userId, 3);
            
            // Get recommended courses (courses not enrolled in)
            List<Course> recommendedCourses = dashboardService.getRecommendedCourses(userId, 4);
            
            // Get progress overview
            Map<String, Object> progressOverview = dashboardService.getProgressOverview(userId);
            
            model.addAttribute("pageTitle", "Dashboard - Learner Portal");
            model.addAttribute("userName", userName);
            model.addAttribute("userId", userId);
            model.addAttribute("stats", stats);
            model.addAttribute("recentEnrollments", recentEnrollments);
            model.addAttribute("topCertifications", topCertifications);
            model.addAttribute("recommendedCourses", recommendedCourses);
            model.addAttribute("progressOverview", progressOverview);
            model.addAttribute("activePage", "dashboard"); // For navbar active state
            
            return "dashboard";
            
        } catch (Exception e) {
            log.error("Error loading dashboard for user: {}", userId, e);
            model.addAttribute("error", "Error loading dashboard data");
            return "error";
        }
    }
    
    /**
     * My Courses - Detailed view of enrolled courses
     */
    @GetMapping("/my-courses")
    public String showMyCourses(@RequestParam(value = "status", required = false) String status,
                              HttpSession session,
                              Model model) {
        
        Integer userId = (Integer) session.getAttribute("userId");
        String userName = (String) session.getAttribute("userName");
        
        if (userId == null || userName == null) {
            return "redirect:/login";
        }
        
        log.info("Loading my courses for user: {} with status filter: {}", userName, status);
        
        try {
            // Get enrollments with optional status filter
            // Get enrollments with optional status filter
            List<Enrollment> enrollments = studentService.getUserEnrollments(userId);
            // Ensure completionPercentage is never null
            for (Enrollment enrollment : enrollments) {
                if (enrollment.getCompletionPercentage() == null) {
                    enrollment.setCompletionPercentage(java.math.BigDecimal.ZERO);
                }
            }
                try {
                    statusFilter = Enrollment.EnrollmentStatus.valueOf(status);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid status filter: {}", status);
                }
            }
            
            List<Enrollment> enrollments = studentService.getUserEnrollments(userId);
            
            // Filter by status if provided
            if (statusFilter != null) {
                final Enrollment.EnrollmentStatus finalStatusFilter = statusFilter;
                enrollments = enrollments.stream()
                    .filter(enrollment -> enrollment.getStatus() == finalStatusFilter)
                    .toList();
            }
            
            // Get course statistics
            Map<String, Long> courseStats = dashboardService.getCourseStatistics(userId);
            
            model.addAttribute("pageTitle", "My Courses - Learner Portal");
            model.addAttribute("userName", userName);
            model.addAttribute("enrollments", enrollments);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("courseStats", courseStats);
            model.addAttribute("activePage", "my-courses"); // For navbar active state
            
            return "my-courses";
            
        } catch (Exception e) {
            log.error("Error loading my courses for user: {}", userId, e);
            model.addAttribute("error", "Error loading your courses");
            return "error";
        }
    }
    
    /**
     * Certificates - View earned certificates
     */
    @GetMapping("/certifications")
    public String showCertifications(HttpSession session, Model model) {
        
        Integer userId = (Integer) session.getAttribute("userId");
        String userName = (String) session.getAttribute("userName");
        
        if (userId == null || userName == null) {
            return "redirect:/login";
        }
        
        log.info("Loading certifications for user: {}", userName);
        
        try {
            // Get all certifications for the user
            List<Certification> certifications = dashboardService.getAllCertifications(userId);
            
            // Get certification statistics
            Map<String, Object> certStats = dashboardService.getCertificationStatistics(userId);
            
            model.addAttribute("pageTitle", "My Certificates - Learner Portal");
            model.addAttribute("userName", userName);
            model.addAttribute("activePage", "certifications");
            model.addAttribute("certifications", certifications);
            model.addAttribute("certStats", certStats);
            
            return "certifications";
            
        } catch (Exception e) {
            log.error("Error loading certifications for user: {}", userId, e);
            model.addAttribute("error", "Error loading certificates");
            return "error";
        }
    }
    
    /**
     * Profile - User profile management
     */
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        
        Integer userId = (Integer) session.getAttribute("userId");
        String userName = (String) session.getAttribute("userName");
        
        if (userId == null || userName == null) {
            return "redirect:/login";
        }
        
        log.info("Loading profile for user: {}", userName);
        
        try {
            // Get user profile information
            Map<String, Object> userProfile = dashboardService.getUserProfile(userId);
            
            // Get learning statistics
            Map<String, Object> learningStats = dashboardService.getLearningStatistics(userId);
            
            model.addAttribute("pageTitle", "My Profile - Learner Portal");
            model.addAttribute("userName", userName);
            model.addAttribute("userProfile", userProfile);
            model.addAttribute("learningStats", learningStats);
            model.addAttribute("activePage", "profile"); // For navbar active state
            
            return "profile";
            
        } catch (Exception e) {
            log.error("Error loading profile for user: {}", userId, e);
            model.addAttribute("error", "Error loading profile");
            return "error";
        }
    }
}
