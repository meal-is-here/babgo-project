package com.babgo.global.security.jwt;

import com.babgo.domain.user.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT 토큰 생성 및 검증 클래스
 *
 * JWT(JSON Web Token)를 생성하고 검증하는 핵심 클래스입니다.
 * Access Token과 Refresh Token을 생성하고 검증합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    /**
     * TODO: Bean 초기화 후 SecretKey를 생성하는 메소드를 작성해야 합니다
     * - @PostConstruct 어노테이션이 붙어있어 Bean 생성 후 자동 실행됩니다
     * - jwtProperties.getSecret()을 바이트 배열로 변환합니다
     * - Keys.hmacShaKeyFor()를 사용하여 SecretKey를 생성합니다
     */
    @PostConstruct
    public void init() {
        // 구현 필요
    }

    /**
     * TODO: Access Token을 생성하는 메소드를 작성해야 합니다
     * - 사용자 ID, 이메일, 권한 정보를 담습니다
     * - Jwts.builder()를 사용합니다
     * - subject: 사용자 ID
     * - claim "email": 이메일
     * - claim "role": 권한 (예: "ROLE_CUSTOMER")
     * - issuedAt: 현재 시간
     * - expiration: 현재 시간 + accessTokenExpiration
     * - signWith(secretKey): 서명
     *
     * @param userId 사용자 ID
     * @param email 이메일
     * @param role 권한
     * @return Access Token 문자열
     */
    public String generateAccessToken(String userId, String email, UserRole role) {
        // 구현 필요
        return null;
    }

    /**
     * TODO: Refresh Token을 생성하는 메소드를 작성해야 합니다
     * - Access Token과 달리 최소한의 정보만 담습니다
     * - subject: 사용자 ID만 포함
     * - issuedAt: 현재 시간
     * - expiration: 현재 시간 + refreshTokenExpiration
     * - signWith(secretKey): 서명
     *
     * @param userId 사용자 ID
     * @return Refresh Token 문자열
     */
    public String generateRefreshToken(String userId) {
        // 구현 필요
        return null;
    }

    /**
     * TODO: Token에서 사용자 ID를 추출하는 메소드를 작성해야 합니다
     * - Jwts.parser()를 사용합니다
     * - verifyWith(secretKey)로 서명 검증
     * - build().parseSignedClaims(token)으로 파싱
     * - getPayload().getSubject()로 사용자 ID 추출
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public String getUserId(String token) {
        // 구현 필요
        return null;
    }

    /**
     * TODO: Token에서 이메일을 추출하는 메소드를 작성해야 합니다
     * - getClaims(token).get("email", String.class) 사용
     *
     * @param token JWT 토큰
     * @return 이메일
     */
    public String getEmail(String token) {
        // 구현 필요
        return null;
    }

    /**
     * TODO: Token에서 권한을 추출하는 메소드를 작성해야 합니다
     * - getClaims(token).get("role", String.class) 사용
     *
     * @param token JWT 토큰
     * @return 권한 문자열 (예: "ROLE_CUSTOMER")
     */
    public String getRole(String token) {
        // 구현 필요
        return null;
    }

    /**
     * TODO: Token에서 Claims를 추출하는 private 메소드를 작성해야 합니다
     * - Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload() 사용
     *
     * @param token JWT 토큰
     * @return Claims
     */
    private Claims getClaims(String token) {
        // 구현 필요
        return null;
    }

    /**
     * TODO: Token 유효성을 검증하는 메소드를 작성해야 합니다
     * - try-catch로 예외 처리
     * - Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token) 실행
     * - 성공하면 true 반환
     * - 실패하면 (ExpiredJwtException, SecurityException, MalformedJwtException, IllegalArgumentException 등) false 반환
     * - 각 예외마다 적절한 로그 출력 (log.error 사용)
     *
     * @param token JWT 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        // 구현 필요
        return false;
    }

    /**
     * TODO: Token으로부터 Authentication 객체를 생성하는 메소드를 작성해야 합니다
     * - getUserId(), getEmail(), getRole()로 정보 추출
     * - SimpleGrantedAuthority로 권한 객체 생성
     * - UserDetails 객체 생성 (User.builder() 사용)
     * - UsernamePasswordAuthenticationToken 생성하여 반환
     * - Spring Security의 SecurityContext에 저장될 인증 객체입니다
     *
     * @param token JWT 토큰
     * @return Authentication 객체
     */
    public Authentication getAuthentication(String token) {
        // 구현 필요
        return null;
    }

    /**
     * TODO: Token의 남은 유효시간을 밀리초로 반환하는 메소드를 작성해야 합니다
     * - getClaims(token).getExpiration()으로 만료 시간 추출
     * - 만료 시간 - 현재 시간을 계산하여 반환
     * - Redis TTL 설정시 사용됩니다 (초 단위로 변환 필요)
     *
     * @param token JWT 토큰
     * @return 남은 유효시간 (밀리초)
     */
    public Long getTokenExpirationTime(String token) {
        // 구현 필요
        return null;
    }
}