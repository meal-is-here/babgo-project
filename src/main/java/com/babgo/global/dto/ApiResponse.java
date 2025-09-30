package com.babgo.global.dto;

import java.time.LocalDateTime;

import com.babgo.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ApiResponse<T> {
	private final boolean success;
	private final String message;
	private final T data;
	@JsonInclude(JsonInclude.Include.NON_NULL)    // null일 때 JSON에서 제외
	private final String errorCode;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime timestamp;

	@Builder
	private ApiResponse(boolean success, String message, T data, String errorCode, LocalDateTime timestamp) {
		this.success = success;
		this.message = message;
		this.data = data;
		this.errorCode = errorCode;
		this.timestamp = timestamp;
	}

	// 성공 응답용
	public static <T> ApiResponse<T> success(String message, T data) {
		return ApiResponse.<T>builder()
						  .success(true)
						  .message(message)
						  .data(data)
						  .timestamp(LocalDateTime.now())
						  .build();
	}

	public static <T> ApiResponse<T> success(String message) {
		return ApiResponse.<T>builder()
						  .success(true)
						  .message(message)
						  .data(null)
						  .timestamp(LocalDateTime.now())
						  .build();
	}

	// 실패 응답용
	public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
		return ApiResponse.<T>builder()
						  .success(false)
						  .message(errorCode.getMessage())
						  .data(null)
						  .errorCode(errorCode.name())
						  .timestamp(LocalDateTime.now())
						  .build();
	}

	// 커스텀 실패 응답용
	public static <T> ApiResponse<T> fail(String message, ErrorCode errorCode) {
		return ApiResponse.<T>builder()
						  .success(false)
						  .message(message)
						  .data(null)
						  .errorCode(errorCode.name())
						  .timestamp(LocalDateTime.now())
						  .build();
	}
}
