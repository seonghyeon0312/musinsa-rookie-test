package com.musinsa.course.exception;

public class DuplicateEnrollmentException extends RuntimeException {

    public DuplicateEnrollmentException(String courseName) {
        super("이미 수강신청한 강좌입니다 ('" + courseName + "')");
    }
}
