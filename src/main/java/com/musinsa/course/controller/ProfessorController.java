package com.musinsa.course.controller;

import com.musinsa.course.dto.ApiResponse;
import com.musinsa.course.dto.ProfessorResponse;
import com.musinsa.course.service.ProfessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfessorController {

    private final ProfessorService professorService;

    public ProfessorController(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @Operation(
            summary = "교수 목록 조회",
            parameters = {
                    @Parameter(name = "page", description = "페이지 번호(0부터)", example = "0"),
                    @Parameter(name = "size", description = "페이지 크기", example = "20"),
                    @Parameter(name = "sort", description = "정렬: 필드,방향 (예: id,DESC)", example = "id,DESC")
            }
    )
    @GetMapping("/professors")
    public ResponseEntity<ApiResponse<Page<ProfessorResponse>>> getProfessors(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProfessorResponse> professors = professorService.getProfessors(normalizeSort(pageable));
        return ResponseEntity.ok(ApiResponse.success(professors));
    }

    /**
     * sort=DESC 같은 잘못된 요청을 id 기준 정렬로 보정한다.
     */
    private Pageable normalizeSort(Pageable pageable) {
        if (pageable == null || pageable.getSort().isUnsorted()) {
            return pageable;
        }

        Sort.Order first = pageable.getSort().iterator().next();
        String rawProp = first.getProperty();
        String prop = rawProp.replaceAll("[\\[\\]\"]", "");

        if ("asc".equalsIgnoreCase(prop) || "desc".equalsIgnoreCase(prop)) {
            Sort.Direction direction = Sort.Direction.fromString(prop);
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(direction, "id"));
        }

        // 허용되지 않은 속성이라면 기본 정렬(id ASC)로 보정
        if (!isAllowedProperty(prop)) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Direction.ASC, "id"));
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(first.getDirection(), prop));
    }

    private boolean isAllowedProperty(String prop) {
        return prop != null && switch (prop) {
            case "id", "name" -> true;
            default -> false;
        };
    }
}
