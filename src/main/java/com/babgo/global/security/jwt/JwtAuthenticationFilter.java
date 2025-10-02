package com.babgo.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
 *
 * 모든 HTTP 요청을 가로채서 JWT 토큰을 검증합니다.
 * 유효한 토큰이면 SecurityContext에 인증 정보를 저장합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 요청을 필터링하는 메인 메소드
     * - OncePerRequestFilter를 상속받아 요청당 한 번만 실행됩니다
     *
     * 처리 순서:
     * 1. resolveToken()으로 요청에서 JWT 토큰 추출
     * 2. 토큰이 있고 유효하면(validateToken) Authentication 객체 생성
     * 3. SecurityContextHolder에 Authentication 저장
     * 4. filterChain.doFilter()로 다음 필터 실행
     * 5. 예외 발생시 로그 출력
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param filterChain FilterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // 1. 요청에서 JWT 토큰 추출
            String token = resolveToken(request);

            // 2. 토큰이 있고 유효하면
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                // 3. Authentication 객체 생성
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                // 4. SecurityContext에 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Security Context에 인증 정보 저장: {}", authentication.getName());
            }
        } catch (Exception e) {
            log.error("Security Context에 인증 정보를 설정할 수 없습니다: {}", e.getMessage());
        }

        // 5. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청의 Header에서 JWT 토큰을 추출하는 메소드
     * - "Authorization" 헤더에서 토큰 추출
     * - "Bearer {token}" 형식으로 전달됨
     * - "Bearer " 접두사를 제거하고 토큰만 반환
     *
     * 처리 순서:
     * 1. request.getHeader("Authorization")으로 헤더 값 추출
     * 2. StringUtils.hasText()로 값이 있는지 확인
     * 3. startsWith("Bearer ")인지 확인
     * 4. substring(7)로 "Bearer " 제거 후 토큰 반환
     * 5. 조건 불만족시 null 반환
     *
     * @param request HttpServletRequest
     * @return JWT 토큰 문자열 또는 null
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}