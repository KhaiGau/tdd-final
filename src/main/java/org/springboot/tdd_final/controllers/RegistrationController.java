package org.springboot.tdd_final.controllers;

import org.springboot.tdd_final.models.Course;
import org.springboot.tdd_final.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RegistrationController {

    private final RegistrationService registrationService;

    @Autowired
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerCourse(@RequestParam String email, @RequestParam Long courseId) {
        try {
            List<Course> upcomingCourses = registrationService.registerCourse(email, courseId);
            return ResponseEntity.ok(upcomingCourses);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/unregister/{courseId}/{email}")
    public ResponseEntity<?> unregisterCourse(@PathVariable Long courseId, @PathVariable String email) {
        try {
            boolean result = registrationService.unregisterCourse(courseId, email);
            return ResponseEntity.ok("Unregistered successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}