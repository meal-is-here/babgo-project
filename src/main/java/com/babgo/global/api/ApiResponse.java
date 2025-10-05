package com.babgo.global.api;

import java.time.LocalDateTime;

import com.babgo.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;

// 사용중
@Getter
public class ApiResponse<T> {
	private final boolean success;
	private final String message;
	private final T data;
	@JsonInclude(JsonInclude.Include.NON_NULL)    // null일 때 JSON에서 제외
	private final String errorCode;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private final LocalDateTime timestamp;

	private ApiResponse(boolean success, String message, T data, String errorCode, LocalDateTime timestamp) {
		this.success = success;
		this.message = message;
		this.data = data;
		this.errorCode = errorCode;
		this.timestamp = timestamp;
	}

	// 성공 응답용
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, "성공", data, null, LocalDateTime.now());
	}

	public static <T> ApiResponse<T> success(String message) {
		return new ApiResponse<>(true, message, null, null, LocalDateTime.now());
	}

	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>(true, message, data, null, LocalDateTime.now());
	}

	// 실패 응답용
	public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
		return new ApiResponse<>(false, errorCode.getMessage(), null, errorCode.name(), LocalDateTime.now());
	}

	// 커스텀 실패 응답용
	public static <T> ApiResponse<T> fail(String message, ErrorCode errorCode) {
		return new ApiResponse<>(false, message, null, errorCode.name(), LocalDateTime.now());
	}
}
