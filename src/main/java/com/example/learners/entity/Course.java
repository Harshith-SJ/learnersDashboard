package com.example.learners.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Integer courseId;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "category")
    private String category;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "prerequisites", columnDefinition = "TEXT")
    private String prerequisites;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "max_capacity")
    private Integer maxCapacity;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CourseStatus status = CourseStatus.upcoming;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "level")
    private CourseLevel level;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "capacity")
    private Integer capacity;
    
    @Column(name = "current_enrollment")
    private Integer currentEnrollment;
    
    @Column(name = "duration_weeks")
    private Integer durationWeeks;
    
    @Column(name = "enrollment_limit")
    private Integer enrollmentLimit;
    
    @Column(name = "instructor_name")
    private String instructorName;
    
    @Column(name = "price")
    private BigDecimal price;
    
    // Relationships
    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private List<CourseMaterial> courseMaterials;
    
    // Enums for status and level
    public enum CourseStatus {
        active, upcoming, archived
    }
    
    public enum CourseLevel {
        Beginner, Intermediate, Advanced
    }
    
    // Helper methods
    public boolean isActive() {
        return status == CourseStatus.active;
    }
    
    public boolean isUpcoming() {
        return status == CourseStatus.upcoming;
    }
    
    public boolean hasAvailableSlots() {
        if (maxCapacity == null) return true; // Unlimited capacity
        int current = (currentEnrollment != null) ? currentEnrollment : 0;
        return current < maxCapacity;
    }
    
    public String getFormattedDuration() {
        if (durationWeeks == null) return "Duration not specified";
        return durationWeeks + " week" + (durationWeeks > 1 ? "s" : "");
    }
}
