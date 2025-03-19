package org.springboot.tdd_final;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springboot.tdd_final.models.Course;
import org.springboot.tdd_final.models.Registration;
import org.springboot.tdd_final.models.Student;
import org.springboot.tdd_final.repository.CourseRepository;
import org.springboot.tdd_final.repository.RegistrationRepository;
import org.springboot.tdd_final.repository.StudentRepository;
import org.springboot.tdd_final.service.RegistrationService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RegistrationServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @InjectMocks
    private RegistrationService registrationService;

    private Student student;
    private Course upcomingCourse1;
    private Course upcomingCourse2;
    private Course upcomingCourse3;
    private Course ongoingCourse1;
    private Course ongoingCourse2;
    private Course pastCourse;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        LocalDateTime now = LocalDateTime.now();

        student = new Student();
        student.setId(1L);
        student.setEmail("test@example.com");
        student.setFirstName("Test");
        student.setLastName("User");

        // Setup upcoming courses (not started yet)
        upcomingCourse1 = new Course();
        upcomingCourse1.setId(1L);
        upcomingCourse1.setName("Java Basics");
        upcomingCourse1.setStartTime(now.plusDays(10));
        upcomingCourse1.setEndTime(now.plusDays(30));
        upcomingCourse1.setPrice(1000000L);

        upcomingCourse2 = new Course();
        upcomingCourse2.setId(2L);
        upcomingCourse2.setName("Spring Framework");
        upcomingCourse2.setStartTime(now.plusDays(5));
        upcomingCourse2.setEndTime(now.plusDays(25));
        upcomingCourse2.setPrice(1500000L);

        upcomingCourse3 = new Course();
        upcomingCourse3.setId(3L);
        upcomingCourse3.setName("Database Design");
        upcomingCourse3.setStartTime(now.plusDays(15));
        upcomingCourse3.setEndTime(now.plusDays(45));
        upcomingCourse3.setPrice(1200000L);

        // Setup ongoing courses
        ongoingCourse1 = new Course();
        ongoingCourse1.setId(4L);
        ongoingCourse1.setName("Python Basics");
        ongoingCourse1.setStartTime(now.minusDays(5));
        ongoingCourse1.setEndTime(now.plusDays(15));
        ongoingCourse1.setPrice(900000L);

        ongoingCourse2 = new Course();
        ongoingCourse2.setId(5L);
        ongoingCourse2.setName("Web Development");
        ongoingCourse2.setStartTime(now.minusDays(3));
        ongoingCourse2.setEndTime(now.plusDays(27));
        ongoingCourse2.setPrice(1300000L);

        // Setup past course
        pastCourse = new Course();
        pastCourse.setId(6L);
        pastCourse.setName("HTML CSS");
        pastCourse.setStartTime(now.minusDays(45));
        pastCourse.setEndTime(now.minusDays(15));
        pastCourse.setPrice(800000L);
    }
    @AfterEach
    public void teardown() {

    }

    @Test
    public void testRegisterCourse_FirstCourse_NoDiscount() {
        // Given
        when(studentRepository.findByEmail(anyString())).thenReturn(of(student));
        when(courseRepository.findById(anyLong())).thenReturn(of(upcomingCourse1));
        when(registrationRepository.findOngoingCoursesByStudentId(anyLong())).thenReturn(List.of());
        when(registrationRepository.findUpcomingCoursesByStudentId(anyLong())).thenReturn(List.of(upcomingCourse1));

        // When
        List<Course> result = registrationService.registerCourse(student.getEmail(), upcomingCourse1.getId());

        // Then
        verify(registrationRepository, times(1)).save(any(Registration.class));
        assertEquals(1, result.size());
        assertEquals(upcomingCourse1.getId(), result.get(0).getId());
    }

    @Test
    public void testRegisterCourse_WithDiscount_StudyingTwoCourses() {
        // Given
        when(studentRepository.findByEmail(student.getEmail())).thenReturn(of(student));
        when(courseRepository.findById(upcomingCourse1.getId())).thenReturn(of(upcomingCourse1));
        when(registrationRepository.findOngoingCoursesByStudentId(student.getId())).thenReturn(List.of(ongoingCourse1, ongoingCourse2));
        when(registrationRepository.findUpcomingCoursesByStudentId(student.getId())).thenReturn(List.of(upcomingCourse1));

        // When
        List<Course> result = registrationService.registerCourse(student.getEmail(), upcomingCourse1.getId());

        // Then
        verify(registrationRepository, times(1)).save(argThat(registration ->
                registration.getPrice() ==(upcomingCourse1.getPrice() * 0.75)
        ));
        //assertEquals(1, result.size());
        assertEquals(upcomingCourse1.getId(), result.get(0).getId());
    }

    @Test
    public void testRegisterCourse_CourseAlreadyStarted() {
        // Given
        when(studentRepository.findByEmail(student.getEmail())).thenReturn(of(student));
        when(courseRepository.findById(ongoingCourse1.getId())).thenReturn(of(ongoingCourse1));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            registrationService.registerCourse(student.getEmail(), ongoingCourse1.getId());
        });
        verify(registrationRepository, never()).save(any(Registration.class));
    }

    @Test
    public void testUnregisterCourse_Success() {
        // Given
        when(studentRepository.findByEmail(student.getEmail())).thenReturn(of(student));
        when(courseRepository.findById(upcomingCourse1.getId())).thenReturn(of(upcomingCourse1));
        when(registrationRepository.findByStudentIdAndCourseId(student.getId(), upcomingCourse1.getId()))
                .thenReturn(of(new Registration()));

        // When
        boolean result = registrationService.unregisterCourse(upcomingCourse1.getId(), student.getEmail());

        // Then
        assertTrue(result);
        verify(registrationRepository, times(1)).deleteByStudentIdAndCourseId(student.getId(), upcomingCourse1.getId());
    }

    @Test
    public void testUnregisterCourse_NotRegistered() {
        // Given
        when(studentRepository.findByEmail(student.getEmail())).thenReturn(of(student));
        when(courseRepository.findById(upcomingCourse1.getId())).thenReturn(of(upcomingCourse1));
        when(registrationRepository.findByStudentIdAndCourseId(student.getId(), upcomingCourse1.getId()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            registrationService.unregisterCourse(upcomingCourse1.getId(), student.getEmail());
        });
        verify(registrationRepository, never()).deleteByStudentIdAndCourseId(anyLong(), anyLong());
    }

    @Test
    public void testUnregisterCourse_CourseAlreadyStarted() {
        // Given
        when(studentRepository.findByEmail(student.getEmail())).thenReturn(of(student));
        when(courseRepository.findById(ongoingCourse1.getId())).thenReturn(of(ongoingCourse1));
        when(registrationRepository.findByStudentIdAndCourseId(student.getId(), ongoingCourse1.getId()))
                .thenReturn(of(new Registration()));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            registrationService.unregisterCourse(ongoingCourse1.getId(), student.getEmail());
        });
        verify(registrationRepository, never()).deleteByStudentIdAndCourseId(anyLong(), anyLong());
    }
}
