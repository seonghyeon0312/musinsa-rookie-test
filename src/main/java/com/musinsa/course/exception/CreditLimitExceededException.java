package com.musinsa.course.exception;

public class CreditLimitExceededException extends RuntimeException {

    public CreditLimitExceededException(int currentCredits, int requestedCredits) {
        super("최대 18학점까지만 신청 가능합니다 (현재: " + currentCredits + "학점, 신청: " + requestedCredits + "학점)");
    }
}
