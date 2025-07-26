package com.example.learners.service;

import com.example.learners.entity.Course;
import com.example.learners.entity.Enrollment;
import com.example.learners.entity.Certification;
import com.example.learners.entity.User;
import com.example.learners.repository.CourseRepository;
import com.example.learners.repository.EnrollmentRepository;
import com.example.learners.repository.CertificationRepository;
import com.example.learners.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {
    
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CertificationRepository certificationRepository;
    private final UserRepository userRepository;
    
    /**
     * Get comprehensive dashboard statistics for a user
     */
    public Map<String, Object> getDashboardStats(Integer userId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Total available courses - count by active status
            long totalCourses = courseRepository.countByStatus(Course.CourseStatus.active);
            
            // User's enrollment statistics
            long enrolledCourses = enrollmentRepository.countByStudentUserId(userId);
            long completedCourses = enrollmentRepository.countByStudentUserIdAndStatus(
                userId, Enrollment.EnrollmentStatus.completed);
            long inProgressCourses = enrollmentRepository.countByStudentUserIdAndStatus(
                userId, Enrollment.EnrollmentStatus.in_progress);
            long assignedCourses = enrollmentRepository.countByStudentUserIdAndStatus(
                userId, Enrollment.EnrollmentStatus.assigned);
            
            // Certifications earned
            long certificationsEarned = certificationRepository.countByStudentUserId(userId);
            
            stats.put("totalCourses", totalCourses);
            stats.put("enrolledCourses", enrolledCourses);
            stats.put("completedCourses", completedCourses);
            stats.put("inProgressCourses", inProgressCourses);
            stats.put("assignedCourses", assignedCourses);
            stats.put("certificationsEarned", certificationsEarned);
            
            // Calculate completion percentage
            double completionRate = enrolledCourses > 0 ? 
                (double) completedCourses / enrolledCourses * 100 : 0;
            stats.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
            
            log.debug("Dashboard stats calculated for user {}: {}", userId, stats);
            
        } catch (Exception e) {
            log.error("Error calculating dashboard stats for user: {}", userId, e);
            // Return default stats on error
            stats.put("totalCourses", 0L);
            stats.put("enrolledCourses", 0L);
            stats.put("completedCourses", 0L);
            stats.put("inProgressCourses", 0L);
            stats.put("assignedCourses", 0L);
            stats.put("certificationsEarned", 0L);
            stats.put("completionRate", 0.0);
        }
        
        return stats;
    }
    
    /**
     * Get recent enrollments for dashboard
     */
    public List<Enrollment> getRecentEnrollments(Integer userId, int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            return enrollmentRepository.findRecentEnrollmentsByStudentId(userId, pageable);
        } catch (Exception e) {
            log.error("Error fetching recent enrollments for user: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Get recent certifications
     */
    public List<Certification> getRecentCertifications(Integer userId, int limit) {
        try {
            return certificationRepository.findByStudentUserId(userId).stream()
                .limit(limit)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching recent certifications for user: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Get recommended courses (courses user is not enrolled in)
     */
    public List<Course> getRecommendedCourses(Integer userId, int limit) {
        try {
            // Get all active courses using existing method
            List<Course> allCourses = courseRepository.findByStatusOrderByCreatedAtDesc(Course.CourseStatus.active);
            
            // Get user's enrolled course IDs
            List<Enrollment> userEnrollments = enrollmentRepository.findStudentEnrollments(userId, null);
            Set<Integer> enrolledCourseIds = userEnrollments.stream()
                .map(enrollment -> enrollment.getCourse().getCourseId())
                .collect(Collectors.toSet());
            
            // Filter out enrolled courses and limit results
            return allCourses.stream()
                .filter(course -> !enrolledCourseIds.contains(course.getCourseId()))
                .limit(limit)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error fetching recommended courses for user: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Get progress overview for dashboard
     */
    public Map<String, Object> getProgressOverview(Integer userId) {
        Map<String, Object> progress = new HashMap<>();
        
        try {
            List<Enrollment> enrollments = enrollmentRepository.findStudentEnrollments(userId, null);
            
            if (enrollments.isEmpty()) {
                progress.put("hasEnrollments", false);
                progress.put("averageProgress", 0.0);
                progress.put("totalHours", 0);
                return progress;
            }
            
            progress.put("hasEnrollments", true);
            
            // Calculate average completion percentage
            double averageProgress = enrollments.stream()
                .filter(e -> e.getCompletionPercentage() != null)
                .mapToDouble(e -> e.getCompletionPercentage().doubleValue())
                .average()
                .orElse(0.0);
            
            progress.put("averageProgress", Math.round(averageProgress * 100.0) / 100.0);
            
            // Estimate total learning hours (assume 2 hours per week per course for duration)
            int totalHours = enrollments.stream()
                .mapToInt(e -> {
                    Course course = e.getCourse();
                    if (course.getDurationWeeks() != null) {
                        return course.getDurationWeeks() * 2; // 2 hours per week
                    }
                    return 8; // default 8 hours if no duration specified
                })
                .sum();
            
            progress.put("totalHours", totalHours);
            
            // Recent activity
            long recentActivity = enrollments.stream()
                .filter(e -> e.getEnrollmentDate() != null)
                .filter(e -> e.getEnrollmentDate().isAfter(LocalDate.now().minusDays(30)))
                .count();
            
            progress.put("recentActivity", recentActivity);
            
        } catch (Exception e) {
            log.error("Error calculating progress overview for user: {}", userId, e);
            progress.put("hasEnrollments", false);
            progress.put("averageProgress", 0.0);
            progress.put("totalHours", 0);
            progress.put("recentActivity", 0L);
        }
        
        return progress;
    }
    
    /**
     * Get course statistics for my-courses page
     */
    public Map<String, Long> getCourseStatistics(Integer userId) {
        Map<String, Long> stats = new HashMap<>();
        
        try {
            stats.put("total", enrollmentRepository.countByStudentUserId(userId));
            stats.put("assigned", enrollmentRepository.countByStudentUserIdAndStatus(userId, Enrollment.EnrollmentStatus.assigned));
            stats.put("inProgress", enrollmentRepository.countByStudentUserIdAndStatus(userId, Enrollment.EnrollmentStatus.in_progress));
            stats.put("completed", enrollmentRepository.countByStudentUserIdAndStatus(userId, Enrollment.EnrollmentStatus.completed));
            stats.put("overdue", enrollmentRepository.countByStudentUserIdAndStatus(userId, Enrollment.EnrollmentStatus.overdue));
        } catch (Exception e) {
            log.error("Error calculating course statistics for user: {}", userId, e);
            stats.put("total", 0L);
            stats.put("assigned", 0L);
            stats.put("inProgress", 0L);
            stats.put("completed", 0L);
            stats.put("overdue", 0L);
        }
        
        return stats;
    }
    
    /**
     * Get all certifications for a user
     */
    public List<Certification> getAllCertifications(Integer userId) {
        try {
            return certificationRepository.findByStudentUserId(userId);
        } catch (Exception e) {
            log.error("Error fetching all certifications for user: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Get certification statistics
     */
    public Map<String, Object> getCertificationStatistics(Integer userId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            List<Certification> certifications = getAllCertifications(userId);
            
            stats.put("totalCertifications", certifications.size());
            
            // Certifications earned this year
            long thisYear = certifications.stream()
                .filter(cert -> cert.getIssueDate() != null)
                .filter(cert -> cert.getIssueDate().getYear() == LocalDate.now().getYear())
                .count();
            
            stats.put("thisYear", thisYear);
            
            // Most recent certification date
            Optional<LocalDate> mostRecent = certifications.stream()
                .filter(cert -> cert.getIssueDate() != null)
                .map(Certification::getIssueDate)
                .max(LocalDate::compareTo);
            
            if (mostRecent.isPresent()) {
                stats.put("mostRecentDate", mostRecent.get().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            } else {
                stats.put("mostRecentDate", "None");
            }
            
        } catch (Exception e) {
            log.error("Error calculating certification statistics for user: {}", userId, e);
            stats.put("totalCertifications", 0);
            stats.put("thisYear", 0L);
            stats.put("mostRecentDate", "None");
        }
        
        return stats;
    }
    
    /**
     * Get user profile information
     */
    public Map<String, Object> getUserProfile(Integer userId) {
        Map<String, Object> profile = new HashMap<>();
        
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                profile.put("firstName", user.getFirstName());
                profile.put("lastName", user.getLastName());
                profile.put("email", user.getEmail());
                profile.put("phoneNumber", user.getPhoneNumber());
                profile.put("age", user.getAge());
                profile.put("location", user.getLocation());
                profile.put("experience", user.getExperience());
                profile.put("createdAt", user.getCreatedAt());
                profile.put("updatedAt", user.getUpdatedAt());
                
                // Member since
                if (user.getCreatedAt() != null) {
                    profile.put("memberSince", user.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM yyyy")));
                } else {
                    profile.put("memberSince", "Unknown");
                }
            }
        } catch (Exception e) {
            log.error("Error fetching user profile for user: {}", userId, e);
        }
        
        return profile;
    }
    
    /**
     * Get learning statistics for profile page
     */
    public Map<String, Object> getLearningStatistics(Integer userId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Get basic statistics
            Map<String, Object> dashboardStats = getDashboardStats(userId);
            stats.putAll(dashboardStats);
            
            // Calculate additional learning metrics
            List<Enrollment> enrollments = enrollmentRepository.findStudentEnrollments(userId, null);
            
            // Learning streak (days with activity)
            long learningStreak = calculateLearningStreak(enrollments);
            stats.put("learningStreak", learningStreak);
            
            // Favorite subject (most enrolled category)
            String favoriteCategory = findFavoriteCategory(enrollments);
            stats.put("favoriteCategory", favoriteCategory);
            
            // Total learning time estimation
            int totalLearningHours = estimateTotalLearningHours(enrollments);
            stats.put("totalLearningHours", totalLearningHours);
            
        } catch (Exception e) {
            log.error("Error calculating learning statistics for user: {}", userId, e);
        }
        
        return stats;
    }
    
    private long calculateLearningStreak(List<Enrollment> enrollments) {
        // Simple implementation - count enrollments in last 30 days
        return enrollments.stream()
            .filter(e -> e.getEnrollmentDate() != null)
            .filter(e -> e.getEnrollmentDate().isAfter(LocalDate.now().minusDays(30)))
            .count();
    }
    
    private String findFavoriteCategory(List<Enrollment> enrollments) {
        Map<String, Long> categoryCount = enrollments.stream()
            .filter(e -> e.getCourse() != null && e.getCourse().getCategory() != null)
            .collect(Collectors.groupingBy(
                e -> e.getCourse().getCategory(),
                Collectors.counting()
            ));
            
        return categoryCount.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("General");
    }
    
    private int estimateTotalLearningHours(List<Enrollment> enrollments) {
        return enrollments.stream()
            .mapToInt(e -> {
                Course course = e.getCourse();
                if (course.getDurationWeeks() != null) {
                    // Estimate based on completion percentage
                    double completionPercentage = e.getCompletionPercentage() != null ? 
                        e.getCompletionPercentage().doubleValue() / 100.0 : 0.0;
                    return (int) (course.getDurationWeeks() * 2 * completionPercentage);
                }
                return 0;
            })
            .sum();
    }
}
