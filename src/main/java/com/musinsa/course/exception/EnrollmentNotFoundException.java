package com.musinsa.course.exception;

public class EnrollmentNotFoundException extends RuntimeException {

    public EnrollmentNotFoundException(Long id) {
        super("수강신청 내역을 찾을 수 없습니다 (ID: " + id + ")");
    }
}
