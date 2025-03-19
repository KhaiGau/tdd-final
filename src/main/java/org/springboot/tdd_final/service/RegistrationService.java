package org.springboot.tdd_final.service;

import org.springboot.tdd_final.models.Course;
import org.springboot.tdd_final.models.Registration;
import org.springboot.tdd_final.models.Student;
import org.springboot.tdd_final.repository.CourseRepository;
import org.springboot.tdd_final.repository.RegistrationRepository;
import org.springboot.tdd_final.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegistrationService {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final RegistrationRepository registrationRepository;

    @Autowired
    public RegistrationService(CourseRepository courseRepository,
                               StudentRepository studentRepository,
                               RegistrationRepository registrationRepository) {
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.registrationRepository = registrationRepository;
    }

    @Transactional
    public List<Course> registerCourse(String email, Long courseId) {
        // Validate student
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Student with email " + email + " not found"));

        // Validate course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalStateException("Course with id " + courseId + " not found"));

        // Check if course has already started
        if (course.hasStarted()) {
            throw new IllegalStateException("Cannot register for a course that has already started");
        }

        // Check if student is already registered for this course
        List<Course> upcomingCourses = registrationRepository.findUpcomingCoursesByStudentId(student.getId());
        /*if (upcomingCourses.stream().anyMatch(c -> c.getId().equals(courseId))) {
            throw new IllegalStateException("Student is already registered for this course");
        }*/

        // Calculate price with discount if applicable
        Long price = course.getPrice();
        List<Course> ongoingCourses = registrationRepository.findOngoingCoursesByStudentId(student.getId());
        if (ongoingCourses.size() >= 2) {
            // Apply 25% discount
            price = (long) (price * 0.75);
        }

        // Create registration
        Registration registration = new Registration();
        registration.setStudentId(student.getId());
        registration.setCourseId(course.getId());
        registration.setPrice(price);
        registration.setRegisteredDate(LocalDateTime.now());

        registrationRepository.save(registration);

        // Fetch lại danh sách các khóa học sắp tới từ database
        List<Course> updatedUpcomingCourses = registrationRepository.findUpcomingCoursesByStudentId(student.getId());

        return updatedUpcomingCourses;

    }

    @Transactional
    public boolean unregisterCourse(Long courseId, String email) {
        // Validate student
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Student with email " + email + " not found"));

        // Validate course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalStateException("Course with id " + courseId + " not found"));

        // Check if course has already started
        if (course.hasStarted()) {
            throw new IllegalStateException("Cannot unregister from a course that has already started");
        }

        // Check if student is registered for this course
        Registration registration = registrationRepository.findByStudentIdAndCourseId(student.getId(), courseId)
                .orElseThrow(() -> new IllegalStateException("Student is not registered for this course"));

        // Delete registration
        registrationRepository.deleteByStudentIdAndCourseId(student.getId(), courseId);

        return true;
    }
}
