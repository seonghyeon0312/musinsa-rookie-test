package com.musinsa.course.dto;

import com.musinsa.course.domain.Student;
import lombok.Getter;

@Getter
public class StudentResponse {

    private final Long id;
    private final String studentNumber;
    private final String name;
    private final int grade;
    private final String department;

    public StudentResponse(Student student) {
        this.id = student.getId();
        this.studentNumber = student.getStudentNumber();
        this.name = student.getName();
        this.grade = student.getGrade();
        this.department = student.getDepartment().getName();
    }
}
