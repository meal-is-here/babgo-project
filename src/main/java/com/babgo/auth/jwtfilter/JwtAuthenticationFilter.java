package com.babgo.auth.jwtfilter;

import com.babgo.auth.JwtTokenProvider;
import com.babgo.auth.exception.FilterExceptionHandler;
import com.babgo.auth.exception.JwtTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * 모든 HTTP 요청에서 액세스 토큰을 검증하고 SecurityContext에 인증 정보를 설정합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = 7;
    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

    private final JwtTokenProvider jwtTokenProvider;
    private final FilterExceptionHandler filterExceptionHandler;

    /**
     * 모든 HTTP 요청에 대해 JWT 토큰을 검증하고 인증 정보를 설정합니다.
     *
     * @param request     HTTP 요청 객체
     * @param response    HTTP 응답 객체
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 예외
     * @throws IOException      입출력 예외
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // 1. 요청에서 액세스 토큰 추출 (Authorization 헤더 또는 accessToken 쿠키)
            String token = resolveToken(request);

            // 2. 토큰이 있는 경우에만 검증 및 인증 정보 설정
            if (StringUtils.hasText(token)) {
                // validateToken은 예외를 던지므로 유효한 경우에만 아래 코드 실행
                jwtTokenProvider.validateToken(token);
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("인증 성공 - user: {}", authentication.getName());
            }
            // 토큰이 없는 경우는 예외를 던지지 않고 다음 필터로 진행 (인증 불필요한 엔드포인트 허용)

            filterChain.doFilter(request, response);

        } catch (JwtTokenException e) {
            // JWT 관련 예외는 구체적인 에러 코드와 메시지로 응답
            log.warn("JWT 토큰 검증 실패 - path: {}, exception: {}, message: {}",
                    request.getRequestURI(), e.getClass().getSimpleName(), e.getMessage());
            filterExceptionHandler.handleJwtException(request, response, e);
            // 예외 발생 시 필터 체인 중단 (응답이 이미 작성됨)

        } catch (Exception e) {
            // 예상치 못한 예외도 처리
            log.error("인증 처리 중 예상치 못한 예외 - path: {}, exception: {}",
                    request.getRequestURI(), e.getClass().getSimpleName(), e);
            filterExceptionHandler.handleJwtException(request, response, e);
            // 예외 발생 시 필터 체인 중단
        }
    }

    /**
     * HTTP 요청에서 JWT 토큰을 추출합니다.
     * Authorization 헤더를 우선적으로 확인하고, 없으면 쿠키에서 찾습니다.
     *
     * @param request HTTP 요청 객체
     * @return JWT 토큰 문자열, 없으면 null
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX_LENGTH);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}