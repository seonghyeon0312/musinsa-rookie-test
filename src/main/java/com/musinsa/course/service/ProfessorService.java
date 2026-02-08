package com.musinsa.course.service;

import com.musinsa.course.dto.ProfessorResponse;
import com.musinsa.course.repository.ProfessorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProfessorService {

    private final ProfessorRepository professorRepository;

    public ProfessorService(ProfessorRepository professorRepository) {
        this.professorRepository = professorRepository;
    }

    public Page<ProfessorResponse> getProfessors(Pageable pageable) {
        return professorRepository.findAll(pageable)
                .map(ProfessorResponse::new);
    }
}
