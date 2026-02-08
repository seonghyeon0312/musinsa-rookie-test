package com.musinsa.course.controller;

import com.musinsa.course.dto.ApiResponse;
import com.musinsa.course.dto.ProfessorResponse;
import com.musinsa.course.repository.ProfessorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfessorController {

    private final ProfessorRepository professorRepository;

    public ProfessorController(ProfessorRepository professorRepository) {
        this.professorRepository = professorRepository;
    }

    @GetMapping("/professors")
    public ResponseEntity<ApiResponse<Page<ProfessorResponse>>> getProfessors(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProfessorResponse> professors = professorRepository.findAll(pageable)
                .map(ProfessorResponse::new);
        return ResponseEntity.ok(ApiResponse.success(professors));
    }
}
