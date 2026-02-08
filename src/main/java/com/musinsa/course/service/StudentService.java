package com.musinsa.course.service;

import com.musinsa.course.dto.StudentResponse;
import com.musinsa.course.exception.StudentNotFoundException;
import com.musinsa.course.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Page<StudentResponse> getStudents(Pageable pageable) {
        return studentRepository.findAll(pageable)
                .map(StudentResponse::new);
    }

    public StudentResponse getStudentById(Long id) {
        return studentRepository.findById(id)
                .map(StudentResponse::new)
                .orElseThrow(() -> new StudentNotFoundException(id));
    }
}
