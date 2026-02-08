package com.musinsa.course.dto;

import com.musinsa.course.domain.Professor;
import lombok.Getter;

@Getter
public class ProfessorResponse {

    private final Long id;
    private final String name;
    private final String department;

    public ProfessorResponse(Professor professor) {
        this.id = professor.getId();
        this.name = professor.getName();
        this.department = professor.getDepartment().getName();
    }
}
