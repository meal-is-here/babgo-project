package com.babgo.auth.exception;

import com.babgo.global.exception.ErrorCode;
import lombok.Getter;

/**
 * JWT 토큰 관련 기본 예외 클래스
 */
@Getter
public class JwtTokenException extends RuntimeException {

    private final ErrorCode errorCode;

    public JwtTokenException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
