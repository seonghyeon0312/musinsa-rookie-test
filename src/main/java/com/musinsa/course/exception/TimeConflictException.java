package com.musinsa.course.exception;

public class TimeConflictException extends RuntimeException {

    public TimeConflictException(String existingCourseName, String schedule) {
        super("'" + existingCourseName + "'과(와) 시간이 겹칩니다 (" + schedule + ")");
    }
}
