package com.example.learners.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Integer enrollmentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_user_id", nullable = false)
    private User student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;
    
    @Column(name = "completion_percentage")
    private BigDecimal completionPercentage;
    
    @Column(name = "score")
    private BigDecimal score;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EnrollmentStatus status = EnrollmentStatus.assigned;
    
    // One-to-one relationship with certification
    @OneToOne(mappedBy = "enrollment", fetch = FetchType.LAZY)
    private Certification certification;
    
    // Enum for enrollment status
    public enum EnrollmentStatus {
        assigned, in_progress, completed, overdue
    }
    
    // Helper methods
    public boolean isCompleted() {
        return status == EnrollmentStatus.completed;
    }
    
    public boolean isInProgress() {
        return status == EnrollmentStatus.in_progress;
    }
    
    public boolean isAssigned() {
        return status == EnrollmentStatus.assigned;
    }
    
    public boolean isOverdue() {
        return status == EnrollmentStatus.overdue;
    }
    
    public String getProgressPercentage() {
        if (completionPercentage == null) return "0";
        return completionPercentage.intValue() + "%";
    }
    
    public String getScoreDisplay() {
        if (score == null) return "Not graded";
        return score.doubleValue() + "%";
    }
    
    public boolean hasCertification() {
        return certification != null && isCompleted();
    }
}
