package com.babgo.global.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

	private final ErrorCode errorCode;
	private final String customMessage;

	public CustomException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
		this.customMessage = errorCode.getMessage();
	}

	public CustomException(ErrorCode errorCode, String customMessage) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
		this.customMessage = customMessage;
	}
}
