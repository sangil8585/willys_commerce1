package com.loopers.domain.order;

public enum OrderState {
    PENDING("대기중"),
    CREATED("생성됨"),
    COMPLETED("완료됨"),
    CANCELLED("취소됨"),
    FAILED("실패");

    private final String description;

    OrderState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
