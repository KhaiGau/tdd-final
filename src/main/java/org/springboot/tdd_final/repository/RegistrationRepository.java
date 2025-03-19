package org.springboot.tdd_final.repository;

import org.springboot.tdd_final.models.Course;
import org.springboot.tdd_final.models.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    Optional<Registration> findByStudentIdAndCourseId(Long studentId, Long courseId);

    void deleteByStudentIdAndCourseId(Long studentId, Long courseId);

    @Query("SELECT c FROM Course c JOIN Registration r ON c.id = r.courseId " +
            "WHERE r.studentId = :studentId AND c.startTime <= CURRENT_TIMESTAMP AND c.endTime >= CURRENT_TIMESTAMP")
    List<Course> findOngoingCoursesByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT c FROM Course c JOIN Registration r ON c.id = r.courseId " +
            "WHERE r.studentId = :studentId AND c.startTime > CURRENT_TIMESTAMP")
    List<Course> findUpcomingCoursesByStudentId(@Param("studentId") Long studentId);
}
