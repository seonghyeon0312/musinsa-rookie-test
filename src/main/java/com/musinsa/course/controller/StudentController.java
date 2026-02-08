package com.musinsa.course.controller;

import com.musinsa.course.dto.ApiResponse;
import com.musinsa.course.dto.StudentResponse;
import com.musinsa.course.exception.StudentNotFoundException;
import com.musinsa.course.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentController {

    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<Page<StudentResponse>>> getStudents(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<StudentResponse> students = studentRepository.findAll(pageable)
                .map(StudentResponse::new);
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(@PathVariable Long id) {
        StudentResponse student = studentRepository.findById(id)
                .map(StudentResponse::new)
                .orElseThrow(() -> new StudentNotFoundException(id));
        return ResponseEntity.ok(ApiResponse.success(student));
    }
}
