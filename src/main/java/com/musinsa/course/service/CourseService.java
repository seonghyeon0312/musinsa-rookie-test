package com.musinsa.course.service;

import com.musinsa.course.domain.Course;
import com.musinsa.course.dto.CourseResponse;
import com.musinsa.course.exception.CourseNotFoundException;
import com.musinsa.course.repository.CourseRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAllWithDetails().stream()
                .map(CourseResponse::new)
                .toList();
    }

    public List<CourseResponse> getCoursesByDepartment(String departmentName) {
        return courseRepository.findByDepartmentNameWithDetails(departmentName).stream()
                .map(CourseResponse::new)
                .toList();
    }

    public Page<CourseResponse> getCoursesPaged(Pageable pageable) {
        return courseRepository.findAll(pageable)
                .map(CourseResponse::new);
    }

    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
        return new CourseResponse(course);
    }
}
