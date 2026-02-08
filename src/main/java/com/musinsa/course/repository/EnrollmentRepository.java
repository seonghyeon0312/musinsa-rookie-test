package com.musinsa.course.repository;

import com.musinsa.course.domain.Enrollment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentId(Long studentId);

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    @Query("SELECT COALESCE(SUM(c.credits), 0) FROM Enrollment e JOIN e.course c WHERE e.student.id = :studentId")
    int sumCreditsByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.course c JOIN FETCH c.department JOIN FETCH c.professor WHERE e.student.id = :studentId")
    List<Enrollment> findByStudentIdWithCourseDetails(@Param("studentId") Long studentId);
}
