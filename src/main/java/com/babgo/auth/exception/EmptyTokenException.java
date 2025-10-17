package com.babgo.auth.exception;

import com.babgo.global.exception.ErrorCode;

/**
 * JWT 토큰 누락 예외
 * HTTP Status: 401 UNAUTHORIZED
 */
public class EmptyTokenException extends JwtTokenException {

    public EmptyTokenException() {
        super(ErrorCode.EMPTY_TOKEN);
    }
}
