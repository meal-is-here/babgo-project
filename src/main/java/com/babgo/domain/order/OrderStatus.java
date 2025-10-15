package com.babgo.domain.order;

import lombok.Getter;

/**
 * 추후 상태 전이 개발 진행 시 변경 예정
 */
@Getter
public enum OrderStatus {

    PENDING("준비중"),
    CONFIRMED("확정됨"),
    PAYMENT_IN_PROGRESS("결제 중"),
    CANCELLED("취소됨"),
    FAILED("실패");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

}
