package com.babgo.auth.exception;

import com.babgo.global.exception.ErrorCode;

/**
 * JWT 토큰 만료 예외
 * HTTP Status: 401 UNAUTHORIZED
 */
public class ExpiredTokenException extends JwtTokenException {

    public ExpiredTokenException() {
        super(ErrorCode.EXPIRED_TOKEN);
    }
}
