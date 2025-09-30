package com.babgo.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.babgo.global.dto.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	protected ResponseEntity<ApiResponse<Void>> handleBusinessException(CustomException e) {
		ErrorCode errorCode = e.getErrorCode();

		log.warn("[{}] {} (status: {})",
				 errorCode.name(),
				 errorCode.getMessage(),
				 errorCode.getHttpStatus().value(),e);

		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ApiResponse.fail(errorCode));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException e) {
		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(ApiResponse.fail(e.getBindingResult().getFieldErrors().get(0).getDefaultMessage(),
								   ErrorCode.VALIDATION_ERROR));
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	protected ResponseEntity<ApiResponse<Void>> handleHandlerMethodValidationException(
		HandlerMethodValidationException e
	) {
		log.warn("[VALIDATION_ERROR] 핸들러 메서드 유효성 검증 실패", e);

		// 첫 번째 에러 메시지만 추출 (원한다면 전부 리스트로 보여줄 수도 있음)
		String message = e.getAllErrors().stream()
						  .findFirst()
						  .map(error -> error.getDefaultMessage())
						  .orElse("요청이 유효하지 않습니다.");

		return ResponseEntity
			.status(ErrorCode.VALIDATION_ERROR.getHttpStatus())
			.body(ApiResponse.fail(message, ErrorCode.VALIDATION_ERROR));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	protected ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
		log.warn("[METHOD_NOT_ALLOWED] {} (status: 405)", e.getMessage());
		ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ApiResponse.fail(errorCode));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	protected ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(HttpMessageNotReadableException e) {
		log.warn("[BAD_REQUEST] 요청 바디 파싱 실패 (status: 400)");
		ErrorCode errorCode = ErrorCode.BAD_REQUEST;
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ApiResponse.fail(errorCode));
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	protected ResponseEntity<ApiResponse<Void>> handleNotFound(NoHandlerFoundException e) {
		log.warn("[NOT_FOUND] 요청 경로 없음 (status: 404)");
		ErrorCode errorCode = ErrorCode.NOT_FOUND;
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ApiResponse.fail(errorCode));
	}

	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ApiResponse<Void>> handleUnhandledException(Exception e) {
		log.error("[INTERNAL_SERVER_ERROR] 예기치 못한 서버 오류", e);
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ApiResponse.fail(errorCode));
	}
}
