package com.babgo.auth.exception;

import com.babgo.global.exception.ErrorCode;

/**
 * JWT 토큰 형식 오류 예외
 * HTTP Status: 400 BAD_REQUEST
 */
public class InvalidTokenException extends JwtTokenException {

    public InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN);
    }
}
