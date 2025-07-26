package com.example.learners.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "coursematerials")  // Use the table that has data
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseMaterial {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id")
    private Integer materialId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Column(name = "material_url", nullable = false, length = 2048)
    private String materialUrl;
    
    @Column(name = "material_description")
    private String materialDescription;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Helper methods
    public boolean hasDescription() {
        return materialDescription != null && !materialDescription.trim().isEmpty();
    }
    
    public String getDisplayName() {
        return hasDescription() ? materialDescription : "Course Material";
    }
    
    public boolean isValidUrl() {
        return materialUrl != null && !materialUrl.trim().isEmpty();
    }
    
    public String getCourseName() {
        return course != null ? course.getTitle() : "Unknown Course";
    }
}
