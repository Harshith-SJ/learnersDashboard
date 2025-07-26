package com.example.learners.repository;

import com.example.learners.entity.CourseMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseMaterialRepository extends JpaRepository<CourseMaterial, Integer> {
    
    // Find all materials for a specific course
    List<CourseMaterial> findByCourse_CourseIdOrderByCreatedAtDesc(Integer courseId);
    
    // Count materials for a course
    long countByCourse_CourseId(Integer courseId);
    
    // Check if course has materials
    boolean existsByCourse_CourseId(Integer courseId);
}
