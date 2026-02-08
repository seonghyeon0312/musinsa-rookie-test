package com.musinsa.course.exception;

public class CapacityExceededException extends RuntimeException {

    public CapacityExceededException(String courseName, int capacity) {
        super("강좌 정원이 초과되었습니다 ('" + courseName + "' 정원: " + capacity + "명)");
    }
}
