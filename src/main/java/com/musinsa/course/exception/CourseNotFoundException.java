package com.musinsa.course.exception;

public class CourseNotFoundException extends RuntimeException {

    public CourseNotFoundException(Long id) {
        super("강좌를 찾을 수 없습니다 (ID: " + id + ")");
    }
}
