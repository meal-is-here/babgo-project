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
    CANCEL_REQUESTED("취소 요청 중"),
    CANCELED("취소됨"),
    REFUNDED("환불됨"),
    REFUND_REQUESTED("환불 요청중"),
    FAILED("실패"),
    EXPIRED("시간만료");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

}
