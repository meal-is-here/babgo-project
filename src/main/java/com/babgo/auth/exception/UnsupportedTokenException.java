package com.babgo.auth.exception;

import com.babgo.global.exception.ErrorCode;

/**
 * 지원되지 않는 JWT 토큰 예외
 * HTTP Status: 401 UNAUTHORIZED
 */
public class UnsupportedTokenException extends JwtTokenException {

    public UnsupportedTokenException() {
        super(ErrorCode.UNSUPPORTED_TOKEN);
    }
}
