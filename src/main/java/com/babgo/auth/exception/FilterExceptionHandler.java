package com.babgo.auth.exception;

import com.babgo.auth.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 필터에서 발생한 JWT 예외를 HTTP 응답으로 변환하는 핸들러
 * 예외 종류에 따라 적절한 HTTP 상태 코드와 메시지를 반환합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FilterExceptionHandler {

    private final ObjectMapper objectMapper;

    /**
     * JWT 예외를 HTTP 응답으로 변환
     *
     * @param request   HTTP 요청
     * @param response  HTTP 응답
     * @param exception 발생한 예외
     * @throws IOException 입출력 예외
     */
    public void handleJwtException(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception exception
    ) throws IOException {

        HttpStatus httpStatus;
        String errorMessage;
        String errorCode;

        if (exception instanceof JwtTokenException) {
            // JWT 커스텀 예외 처리 (ErrorCode 사용)
            JwtTokenException jwtException = (JwtTokenException) exception;
            httpStatus = jwtException.getErrorCode().getHttpStatus();
            errorMessage = jwtException.getErrorCode().getMessage();
            errorCode = jwtException.getErrorCode().name();
            log.warn("JWT 예외 - path: {}, errorCode: {}, message: {}",
                    request.getRequestURI(), errorCode, errorMessage);

        } else {
            // 500: 예상치 못한 예외
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            errorMessage = "인증 처리 중 오류가 발생했습니다.";
            errorCode = "AUTHENTICATION_ERROR";
            log.error("인증 처리 중 예상치 못한 예외 - path: {}, exception: {}",
                    request.getRequestURI(), exception.getClass().getSimpleName(), exception);
        }

        // HTTP 응답 설정
        response.setStatus(httpStatus.value());
        response.setContentType("application/json;charset=UTF-8");

        // 에러 응답 생성
        ErrorResponse errorResponse = new ErrorResponse(
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                errorCode,
                errorMessage,
                request.getRequestURI()
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
