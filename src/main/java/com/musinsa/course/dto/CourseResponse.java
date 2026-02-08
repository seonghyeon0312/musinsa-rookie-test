package com.musinsa.course.dto;

import com.musinsa.course.domain.Course;
import java.util.List;
import lombok.Getter;

@Getter
public class CourseResponse {

    private final Long id;
    private final String name;
    private final String courseCode;
    private final int credits;
    private final int capacity;
    private final int enrolled;
    private final String schedule;
    private final String professor;
    private final String department;

    public CourseResponse(Course course) {
        this.id = course.getId();
        this.name = course.getName();
        this.courseCode = course.getCourseCode();
        this.credits = course.getCredits();
        this.capacity = course.getCapacity();
        this.enrolled = course.getEnrolled();
        this.schedule = course.getSchedules().stream()
                .map(s -> s.toDisplayString())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        this.professor = course.getProfessor().getName();
        this.department = course.getDepartment().getName();
    }
}
