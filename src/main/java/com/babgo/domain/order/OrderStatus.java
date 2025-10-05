package com.babgo.domain.order;

/**
 * 추후 상태 전이 개발 진행 시 변경 예정
 */
public enum OrderStatus {

    PENDING, //대기중
    PAYMENT, //결제 중
    CONFIRMED, //확정됨
    CANCELLED, //취소됨
    FAILED //실패함

}
