package com.example.learners.repository;

import com.example.learners.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {
    
    // Find all active courses (for course browsing)
    List<Course> findByStatusOrderByCreatedAtDesc(Course.CourseStatus status);
    
    // Find courses by category and status (with filtering)
    List<Course> findByStatusAndCategoryOrderByCreatedAtDesc(Course.CourseStatus status, String category);
    
    // Find courses by level and status
    List<Course> findByStatusAndLevelOrderByCreatedAtDesc(Course.CourseStatus status, Course.CourseLevel level);
    
    // Find courses by category, level and status (multiple filters)
    List<Course> findByStatusAndCategoryAndLevelOrderByCreatedAtDesc(
        Course.CourseStatus status, String category, Course.CourseLevel level);
    
    // Search courses by title or description (for search functionality)
    @Query("SELECT c FROM Course c WHERE c.status = :status AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Course> searchCoursesByKeyword(@Param("status") Course.CourseStatus status, 
                                       @Param("keyword") String keyword);
    
    // Get all distinct categories (for filter dropdown)
    @Query("SELECT DISTINCT c.category FROM Course c WHERE c.category IS NOT NULL AND c.status = :status")
    List<String> findDistinctCategoriesByStatus(@Param("status") Course.CourseStatus status);
    
    // Count total active courses (for dashboard stats)
    long countByStatus(Course.CourseStatus status);
    
    // Count all active courses
    @Query("SELECT COUNT(c) FROM Course c WHERE c.status = 'active'")
    long countActiveCourses();
    
    // Find all active courses
    @Query("SELECT c FROM Course c WHERE c.status = 'active' ORDER BY c.createdAt DESC")
    List<Course> findActiveCourses();
    
    // Find active courses with pagination (for performance)
    Page<Course> findByStatusOrderByCreatedAtDesc(Course.CourseStatus status, Pageable pageable);
    
    // Check if course is available for enrollment
    @Query("SELECT c FROM Course c WHERE c.courseId = :courseId AND c.status IN ('active', 'upcoming') " +
           "AND (c.maxCapacity IS NULL OR c.currentEnrollment < c.maxCapacity)")
    Course findAvailableForEnrollment(@Param("courseId") Integer courseId);
}
