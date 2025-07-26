package com.example.learners.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;
    
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // Add login required interceptor for protected routes
        registry.addInterceptor(loginRequiredInterceptor)
                .addPathPatterns("/dashboard/**", "/courses/**", "/my-courses/**", 
                               "/profile/**", "/certifications/**")
                .excludePathPatterns("/login", "/logout", "/css/**", "/js/**", "/images/**");
    }
}
