package org.springboot.tdd_final;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springboot.tdd_final.controllers.RegistrationController;
import org.springboot.tdd_final.models.Course;
import org.springboot.tdd_final.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class RegistrationControllerTest {

    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private RegistrationController registrationController;

    private Course course1;
    private Course course2;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        LocalDateTime now = LocalDateTime.now();

        course1 = new Course();
        course1.setId(1L);
        course1.setName("Java Basics");
        course1.setStartTime(now.plusDays(10));
        course1.setEndTime(now.plusDays(30));
        course1.setPrice(1000000L);

        course2 = new Course();
        course2.setId(2L);
        course2.setName("Spring Framework");
        course2.setStartTime(now.plusDays(5));
        course2.setEndTime(now.plusDays(25));
        course2.setPrice(1500000L);
    }

    @Test
    public void testRegisterCourse_Success() {
        // Given
        String email = "test@example.com";
        Long courseId = 1L;
        List<Course> upcomingCourses = Arrays.asList(course1, course2);
        when(registrationService.registerCourse(email, courseId)).thenReturn(upcomingCourses);

        // When
        ResponseEntity<?> response = registrationController.registerCourse(email, courseId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(upcomingCourses, response.getBody());
        verify(registrationService, times(1)).registerCourse(email, courseId);
    }

    @Test
    public void testRegisterCourse_Exception() {
        // Given
        String email = "test@example.com";
        Long courseId = 1L;
        String errorMessage = "Registration failed";
        when(registrationService.registerCourse(email, courseId)).thenThrow(new IllegalStateException(errorMessage));

        // When
        ResponseEntity<?> response = registrationController.registerCourse(email, courseId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
        verify(registrationService, times(1)).registerCourse(email, courseId);
    }

    @Test
    public void testUnregisterCourse_Success() {
        // Given
        String email = "test@example.com";
        Long courseId = 1L;
        when(registrationService.unregisterCourse(courseId, email)).thenReturn(true);

        // When
        ResponseEntity<?> response = registrationController.unregisterCourse(courseId, email);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Unregistered successfully", response.getBody());
        verify(registrationService, times(1)).unregisterCourse(courseId, email);
    }

    @Test
    public void testUnregisterCourse_Exception() {
        // Given
        String email = "test@example.com";
        Long courseId = 1L;
        String errorMessage = "Unregistration failed";
        when(registrationService.unregisterCourse(courseId, email)).thenThrow(new IllegalStateException(errorMessage));

        // When
        ResponseEntity<?> response = registrationController.unregisterCourse(courseId, email);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
        verify(registrationService, times(1)).unregisterCourse(courseId, email);
    }
}
