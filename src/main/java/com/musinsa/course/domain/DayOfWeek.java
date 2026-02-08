package com.musinsa.course.domain;

public enum DayOfWeek {
    MON("월"),
    TUE("화"),
    WED("수"),
    THU("목"),
    FRI("금");

    private final String korean;

    DayOfWeek(String korean) {
        this.korean = korean;
    }

    public String getKorean() {
        return korean;
    }
}
