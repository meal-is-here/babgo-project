package com.babgo.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 인증/인가 실패 시 반환되는 에러 응답 DTO
 */
@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    /**
     * 401 Unauthorized 에러 응답 생성
     * @param message 에러 메시지
     * @param path 요청 경로
     * @return ErrorResponse 객체
     */
    public static ErrorResponse unauthorized(String message, String path) {
        return new ErrorResponse(401, "Unauthorized", message, path);
    }

    /**
     * 403 Forbidden 에러 응답 생성
     * @param message 에러 메시지
     * @param path 요청 경로
     * @return ErrorResponse 객체
     */
    public static ErrorResponse forbidden(String message, String path) {
        return new ErrorResponse(403, "Forbidden", message, path);
    }
}
