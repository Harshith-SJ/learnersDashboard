package com.example.learners.service;

import com.example.learners.entity.Course;
import com.example.learners.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService implements ApplicationRunner {
    
    private final CourseRepository courseRepository;
    
    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info("Running data initialization...");
        updateCourseInstructorNames();
    }
    
    private void updateCourseInstructorNames() {
        try {
            // Get all courses that don't have instructor names
            List<Course> courses = courseRepository.findAll()
                .stream()
                .filter(course -> course.getInstructorName() == null || course.getInstructorName().trim().isEmpty())
                .toList();
            
            if (courses.isEmpty()) {
                log.info("All courses already have instructor names assigned");
                return;
            }
            
            // Map course categories to instructor names
            Map<String, String> categoryInstructors = new HashMap<>();
            categoryInstructors.put("Development", "Dr. Sarah Chen");
            categoryInstructors.put("Data Science", "Prof. Michael Rodriguez");
            categoryInstructors.put("Data", "Prof. Michael Rodriguez");
            categoryInstructors.put("DevOps", "John Patterson");
            categoryInstructors.put("Security", "Lisa Thompson");
            categoryInstructors.put("Cloud", "David Kumar");
            
            // Default instructors for different levels
            String[] defaultInstructors = {
                "Jennifer Adams", "Robert Johnson", "Emily Davis", 
                "Mark Wilson", "Anna Martinez", "Chris Anderson"
            };
            
            int instructorIndex = 0;
            
            for (Course course : courses) {
                String instructorName;
                
                // Try to assign based on category first
                if (course.getCategory() != null && categoryInstructors.containsKey(course.getCategory())) {
                    instructorName = categoryInstructors.get(course.getCategory());
                } else {
                    // Use default instructors in rotation
                    instructorName = defaultInstructors[instructorIndex % defaultInstructors.length];
                    instructorIndex++;
                }
                
                course.setInstructorName(instructorName);
                courseRepository.save(course);
                
                log.debug("Updated course '{}' with instructor '{}'", course.getTitle(), instructorName);
            }
            
            log.info("Updated {} courses with instructor names", courses.size());
            
        } catch (Exception e) {
            log.error("Error updating course instructor names", e);
        }
    }
}
