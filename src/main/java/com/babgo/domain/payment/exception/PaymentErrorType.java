package com.babgo.domain.payment.exception;

import com.babgo.global.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorType implements ErrorType {

    CONFLICT(HttpStatus.CONFLICT, "이미 결제가 진행 중이거나 완료되었습니다."),
    UNPROCESSABLE(HttpStatus.UNPROCESSABLE_ENTITY, "현재 주문은 결제할 수 없습니다."),
    AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 주문 금액과 일치하지 않습니다."),
    PG_COMMUNICATION_FAILED(HttpStatus.BAD_GATEWAY, "PG사와의 통신에 실패했습니다."),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "결제 처리 중 알 수 없는 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
