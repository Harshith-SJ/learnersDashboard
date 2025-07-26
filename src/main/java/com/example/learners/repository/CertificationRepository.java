package com.example.learners.repository;

import com.example.learners.entity.Certification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    
    // Find all certifications for a student
    @Query("SELECT c FROM Certification c JOIN FETCH c.enrollment e JOIN FETCH e.course " +
           "WHERE e.student.userId = :studentId ORDER BY c.issueDate DESC")
    List<Certification> findByStudentUserId(@Param("studentId") Integer studentId);
    
    // Alternative method name for dashboard service
    @Query("SELECT c FROM Certification c JOIN FETCH c.enrollment e JOIN FETCH e.course " +
           "WHERE e.student.userId = :studentId ORDER BY c.issueDate DESC")
    List<Certification> findAllByStudentId(@Param("studentId") Integer studentId);
    
    // Find top 3 recent certifications for dashboard
    @Query("SELECT c FROM Certification c JOIN FETCH c.enrollment e JOIN FETCH e.course " +
           "WHERE e.student.userId = :studentId ORDER BY c.issueDate DESC LIMIT 3")
    List<Certification> findTop3ByStudentUserId(@Param("studentId") Integer studentId);
    
    // Find recent certifications with pagination
    @Query("SELECT c FROM Certification c JOIN FETCH c.enrollment e JOIN FETCH e.course " +
           "WHERE e.student.userId = :studentId ORDER BY c.issueDate DESC")
    List<Certification> findTopRecentCertifications(@Param("studentId") Integer studentId, Pageable pageable);
    
    // Count total certifications for a student (dashboard stats)
    @Query("SELECT COUNT(c) FROM Certification c JOIN c.enrollment e WHERE e.student.userId = :studentId")
    long countByStudentUserId(@Param("studentId") Integer studentId);
    
    // Alternative method name for dashboard service
    @Query("SELECT COUNT(c) FROM Certification c JOIN c.enrollment e WHERE e.student.userId = :studentId")
    long countByStudentId(@Param("studentId") Integer studentId);
    
    // Find certifications by course category (for filtering)
    @Query("SELECT c FROM Certification c JOIN FETCH c.enrollment e JOIN FETCH e.course co " +
           "WHERE e.student.userId = :studentId AND co.category = :category ORDER BY c.issueDate DESC")
    List<Certification> findByStudentUserIdAndCourseCategory(@Param("studentId") Integer studentId, 
                                                            @Param("category") String category);
    
    // Check if certification exists for enrollment
    boolean existsByEnrollmentEnrollmentId(Integer enrollmentId);
}
