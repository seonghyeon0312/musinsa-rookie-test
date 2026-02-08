package com.musinsa.course.repository;

import com.musinsa.course.domain.Course;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Course c WHERE c.id = :id")
    Optional<Course> findByIdWithLock(@Param("id") Long id);

    List<Course> findByDepartmentId(Long departmentId);

    @Query("SELECT c FROM Course c JOIN FETCH c.department JOIN FETCH c.professor")
    List<Course> findAllWithDetails();

    @Query("SELECT c FROM Course c JOIN FETCH c.department JOIN FETCH c.professor WHERE c.department.name = :departmentName")
    List<Course> findByDepartmentNameWithDetails(@Param("departmentName") String departmentName);
}
