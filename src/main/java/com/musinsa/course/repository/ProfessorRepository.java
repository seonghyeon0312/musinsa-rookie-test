package com.musinsa.course.repository;

import com.musinsa.course.domain.Professor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    List<Professor> findByDepartmentId(Long departmentId);
}
