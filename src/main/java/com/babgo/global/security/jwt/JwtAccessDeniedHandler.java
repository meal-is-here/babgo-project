package com.babgo.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 권한 부족 처리 핸들러
 *
 * 인증은 되었지만 권한이 부족한 사용자가 접근할 때 호출됩니다.
 * 403 Forbidden 에러를 JSON 형식으로 반환합니다.
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * TODO: 권한 부족시 호출되는 메소드를 작성해야 합니다
     * - 403 Forbidden 에러 반환
     * - JSON 형식으로 에러 메시지 전달
     *
     * 처리 순서:
     * 1. 로그 출력 (log.error)
     * 2. response.setStatus(HttpServletResponse.SC_FORBIDDEN) - 403 설정
     * 3. response.setContentType("application/json;charset=UTF-8")
     * 4. JSON 응답 생성 (에러 정보 포함)
     *    - status: 403
     *    - error: "Forbidden"
     *    - message: "접근 권한이 없습니다" 또는 accessDeniedException.getMessage()
     *    - path: request.getRequestURI()
     * 5. ObjectMapper로 JSON 변환 후 response.getWriter().write()
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param accessDeniedException 접근 거부 예외
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        // 구현 필요
        // 1. log.error("접근 거부: {}", accessDeniedException.getMessage());
        // 2. response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        // 3. response.setContentType("application/json;charset=UTF-8");
        // 4. Map<String, Object> errorDetails = new HashMap<>();
        //    errorDetails.put("status", 403);
        //    errorDetails.put("error", "Forbidden");
        //    errorDetails.put("message", "접근 권한이 없습니다");
        //    errorDetails.put("path", request.getRequestURI());
        // 5. ObjectMapper objectMapper = new ObjectMapper();
        //    response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }
}