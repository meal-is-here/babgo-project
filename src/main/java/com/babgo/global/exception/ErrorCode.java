package com.babgo.global.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
	INVALID(HttpStatus.BAD_REQUEST, "올바르지 않은 값입니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 경로를 찾을 수 없습니다."),
	NOT_RESOURCE_OWNER(HttpStatus.FORBIDDEN, "해당 리소스 소유자가 아닙니다."),
	EXTERNAL(HttpStatus.BAD_GATEWAY, "외부 시스템 연동 중 오류가 발생했습니다."),
	// Valid
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation Error"),

	// Auth
	INVALID_TOKEN(HttpStatus.BAD_REQUEST, "잘못된 토큰 값입니다."),
	EMPTY_TOKEN(HttpStatus.UNAUTHORIZED, "JWT 토큰이 비어 있습니다."),
	EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "JWT 토큰이 만료되었습니다."),
	UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "지원하지 않는 JWT 토큰입니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

	// User
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
	DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 잘못되었습니다."),
	USER_DELETED(HttpStatus.FORBIDDEN, "탈퇴한 사용자입니다."),
	USER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 탈퇴한 사용자입니다."),
	USER_NOT_DELETED(HttpStatus.BAD_REQUEST, "탈퇴하지 않은 사용자입니다."),
	UNAUTHORIZED_ACTION(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다."),
	TOO_MANY_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "로그인 시도 횟수가 초과되었습니다. 5분 후 다시 시도해주세요."),
	TOO_MANY_SESSIONS(HttpStatus.FORBIDDEN, "동시 접속 가능한 디바이스 수를 초과했습니다."),

	// Profile
	ALREADY_DELETE_USER(HttpStatus.BAD_REQUEST, "이미 삭제된 계정입니다."),
    //store
    STORE_CLOSED(HttpStatus.UNPROCESSABLE_ENTITY, "현재 주문 가능한 상태가 아닙니다." ),

    //menu
    MENU_UNAVAILABLE(HttpStatus.NOT_FOUND, "해당 메뉴를 찾을 수 없습니다."),
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "해당 메뉴의 재고가 부족합니다."),
    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주문을 찾을 수 없습니다."),
    ORDER_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "주문이 완료되지 않았습니다."),
    ORDER_NOT_PAYABLE(HttpStatus.UNPROCESSABLE_ENTITY, "주문 상태상 결제를 진행할 수 없습니다."),
    ORDER_NOT_CANCELABLE(HttpStatus.UNPROCESSABLE_ENTITY, "현재 상태에서는 주문을 취소할 수 없습니다."),
    ORDER_ALREADY_CANCELED(HttpStatus.CONFLICT, "이미 취소된 주문입니다."),
    ORDER_ALREADY_REFUNDED(HttpStatus.CONFLICT, "이미 환불된 주문입니다."),
    CONFLICT(HttpStatus.NOT_FOUND, "해당 주문을 찾을 수 없습니다."),

	// Review
	REVIEW_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 해당 주문에 대한 리뷰가 존재합니다."),

	// Payment
	PAYMENT_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "PG 응답이 시간 초과되었습니다."),
	PAYMENT_ALREADY_IN_PROGRESS(HttpStatus.CONFLICT, "이미 진행 중이거나 완료된 결제입니다."),
	PAYMENT_ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 완료된 결제입니다."),
	PAYMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "결제가 이미 생성되어 있습니다."),
	PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 일치하지 않습니다."),
	PAYMENT_STATUS_CONFLICT(HttpStatus.CONFLICT, "결제 상태 전이 중 충돌이 발생했습니다."),
	PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다.")
	;

	private final HttpStatus httpStatus;
	private final String message;
}
