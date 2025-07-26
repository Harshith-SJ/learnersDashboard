package com.example.learners.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "certifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certificate_id")
    private Long certificateId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", unique = true, nullable = false)
    private Enrollment enrollment;
    
    @Column(name = "certificate_title", nullable = false)
    private String certificateTitle;
    
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;
    
    @Column(name = "certificate_url", length = 2048)
    private String certificateUrl;
    
    // Helper methods
    public String getCourseName() {
        return enrollment != null && enrollment.getCourse() != null 
            ? enrollment.getCourse().getTitle() 
            : "Unknown Course";
    }
    
    public String getStudentName() {
        return enrollment != null && enrollment.getStudent() != null 
            ? enrollment.getStudent().getFullName() 
            : "Unknown Student";
    }
    
    public boolean hasDownloadUrl() {
        return certificateUrl != null && !certificateUrl.trim().isEmpty();
    }
    
    public String getFormattedIssueDate() {
        return issueDate != null ? issueDate.toString() : "Date not available";
    }
}
