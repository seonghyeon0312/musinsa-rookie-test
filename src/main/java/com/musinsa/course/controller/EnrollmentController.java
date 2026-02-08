package com.musinsa.course.controller;

import com.musinsa.course.dto.ApiResponse;
import com.musinsa.course.dto.EnrollmentRequest;
import com.musinsa.course.dto.EnrollmentResponse;
import com.musinsa.course.service.EnrollmentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/enrollments")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enroll(
            @Valid @RequestBody EnrollmentRequest request) {
        EnrollmentResponse response = enrollmentService.enroll(
                request.getStudentId(), request.getCourseId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/enrollments/{id}")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable Long id) {
        enrollmentService.cancel(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/students/{studentId}/enrollments")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getStudentEnrollments(
            @PathVariable Long studentId) {
        List<EnrollmentResponse> enrollments = enrollmentService.getStudentEnrollments(studentId);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }
}
