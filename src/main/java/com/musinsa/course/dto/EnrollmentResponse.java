package com.musinsa.course.dto;

import com.musinsa.course.domain.Enrollment;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class EnrollmentResponse {

    private final Long id;
    private final Long studentId;
    private final String studentName;
    private final Long courseId;
    private final String courseName;
    private final int credits;
    private final String schedule;
    private final LocalDateTime enrolledAt;

    public EnrollmentResponse(Enrollment enrollment) {
        this.id = enrollment.getId();
        this.studentId = enrollment.getStudent().getId();
        this.studentName = enrollment.getStudent().getName();
        this.courseId = enrollment.getCourse().getId();
        this.courseName = enrollment.getCourse().getName();
        this.credits = enrollment.getCourse().getCredits();
        this.schedule = enrollment.getCourse().getSchedules().stream()
                .map(s -> s.toDisplayString())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        this.enrolledAt = enrollment.getEnrolledAt();
    }
}
