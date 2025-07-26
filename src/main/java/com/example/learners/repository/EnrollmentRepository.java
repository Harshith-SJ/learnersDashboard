package com.example.learners.repository;

import com.example.learners.entity.Enrollment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {
    
    // CONSOLIDATED: Master query for all student enrollments with optional filters
    @Query("SELECT e FROM Enrollment e " +
           "JOIN FETCH e.course c " +
           "LEFT JOIN FETCH c.courseMaterials cm " +
           "LEFT JOIN FETCH e.certification cert " +
           "WHERE e.student.userId = :studentId " +
           "AND (:status IS NULL OR e.status = :status) " +
           "ORDER BY e.enrollmentDate DESC")
    List<Enrollment> findStudentEnrollments(@Param("studentId") Integer studentId, 
                                          @Param("status") Enrollment.EnrollmentStatus status);
    
    // OPTIMIZED: Get recent enrollments with pagination for dashboard
    @Query("SELECT e FROM Enrollment e " +
           "JOIN FETCH e.course c " +
           "WHERE e.student.userId = :studentId " +
           "AND e.status IN ('assigned', 'in_progress', 'completed') " +
           "ORDER BY e.enrollmentDate DESC")
    List<Enrollment> findRecentEnrollmentsByStudentId(@Param("studentId") Integer studentId, Pageable pageable);
    
    // SIMPLE: Check if student is already enrolled in a course
    @Query("SELECT e FROM Enrollment e WHERE e.student.userId = :studentId AND e.course.courseId = :courseId")
    Optional<Enrollment> findByStudentUserIdAndCourseId(@Param("studentId") Integer studentId, @Param("courseId") Integer courseId);
    
    // EFFICIENT: Count queries for dashboard stats
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.userId = :studentId")
    long countByStudentUserId(@Param("studentId") Integer studentId);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.userId = :studentId AND e.status = :status")
    long countByStudentUserIdAndStatus(@Param("studentId") Integer studentId, 
                                     @Param("status") Enrollment.EnrollmentStatus status);
    
    // VALIDATION: Check enrollment exists
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Enrollment e WHERE e.student.userId = :studentId AND e.course.courseId = :courseId")
    boolean existsByStudentUserIdAndCourseId(@Param("studentId") Integer studentId, @Param("courseId") Integer courseId);
    
    // BATCH: Get multiple enrollments by IDs for batch operations
    @Query("SELECT e FROM Enrollment e JOIN FETCH e.course WHERE e.enrollmentId IN :enrollmentIds")
    List<Enrollment> findByEnrollmentIdsWithCourse(@Param("enrollmentIds") List<Integer> enrollmentIds);
}
