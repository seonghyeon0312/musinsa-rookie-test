package com.musinsa.course.repository;

import com.musinsa.course.domain.CourseSchedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, Long> {

    List<CourseSchedule> findByCourseId(Long courseId);
}
