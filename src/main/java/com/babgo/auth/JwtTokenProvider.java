package com.babgo.auth;

import com.babgo.auth.exception.*;
import com.babgo.auth.jwtfilter.JwtCookiesProperties;
import com.babgo.domain.user.UserRole;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 담당하는 Provider
 * 액세스 토큰과 리프레시 토큰의 생성, 파싱, 검증 기능을 제공합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtCookiesProperties jwtCookiesProperties;
    private final UserDetailsService userDetailsService;
    private SecretKey secretKey;

    /**
     * JWT Secret Key를 초기화합니다.
     * application.yml의 jwt.secret 값을 기반으로 HMAC-SHA 키를 생성합니다.
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = jwtCookiesProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT Secret Key 초기화 완료");
    }

    /**
     * 액세스 토큰을 생성합니다.
     * subject에 userId, claim에 email과 role을 포함하며 15분의 유효기간을 가집니다.
     *
     * @param userId 사용자 ID
     * @param email  사용자 이메일
     * @param role   사용자 권한
     * @return 생성된 액세스 토큰 문자열
     */
    public String generateAccessToken(Long userId, String email, UserRole role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtCookiesProperties.getAccessTokenExpiration());
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role.getKey())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(this.secretKey)
                .compact();
    }

    /**
     * 리프레시 토큰을 생성합니다.
     * subject에 userId, claim에 type=refresh를 포함하며 1일의 유효기간을 가집니다.
     *
     * @param userId 사용자 ID
     * @return 생성된 리프레시 토큰 문자열
     */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtCookiesProperties.getRefreshTokenExpiration());
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(this.secretKey)
                .compact();
    }

    /**
     * 토큰에서 userId를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 사용자 ID 문자열
     */
    public String getUserId(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * 토큰을 파싱하여 Claims를 추출합니다.
     * Secret Key로 서명을 검증한 후 페이로드를 반환합니다.
     *
     * @param token JWT 토큰
     * @return Claims 객체
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 액세스 토큰의 유효성을 검증합니다.
     * 서명, 만료시간, 형식 등을 확인합니다.
     *
     * @param token JWT 액세스 토큰
     * @throws ExpiredTokenException 토큰이 만료된 경우
     * @throws UnsupportedTokenException 지원되지 않는 토큰 형식인 경우
     * @throws InvalidTokenException 토큰 형식이 잘못된 경우
     * @throws EmptyTokenException 토큰이 비어있는 경우
     */
    public void validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("토큰 검증 실패 - 만료된 토큰: {}", e.getMessage());
            throw new ExpiredTokenException();
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.error("토큰 검증 실패 - 지원되지 않는 토큰: {}", e.getMessage());
            throw new UnsupportedTokenException();
        } catch (MalformedJwtException e) {
            log.error("토큰 검증 실패 - 잘못된 형식: {}", e.getMessage());
            throw new InvalidTokenException();
        } catch (SecurityException e) {
            log.error("토큰 검증 실패 - 서명 검증 실패: {}", e.getMessage());
            throw new InvalidTokenException();
        } catch (IllegalArgumentException e) {
            log.error("토큰 검증 실패 - 빈 토큰: {}", e.getMessage());
            throw new EmptyTokenException();
        }
    }

    /**
     * 리프레시 토큰의 유효성을 검증합니다.
     * type=refresh claim 확인 및 서명/만료시간을 검증합니다.
     *
     * @param token JWT 리프레시 토큰
     * @throws ExpiredTokenException 토큰이 만료된 경우
     * @throws InvalidTokenException 리프레시 토큰이 아니거나 검증에 실패한 경우
     */
    public void validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
            if (!"refresh".equals(claims.get("type", String.class))) {
                log.error("토큰 검증 실패 - 리프레시 토큰이 아님");
                throw new InvalidTokenException();
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("토큰 검증 실패 - 만료된 리프레시 토큰: {}", e.getMessage());
            throw new ExpiredTokenException();
        } catch (JwtTokenException e) {
            // 이미 커스텀 예외인 경우 그대로 던짐
            throw e;
        } catch (Exception e) {
            log.error("토큰 검증 실패 - 리프레시 토큰 검증 실패: {}", e.getMessage());
            throw new InvalidTokenException();
        }
    }

    /**
     * 리프레시 토큰에서 userId를 추출합니다.
     *
     * @param token JWT 리프레시 토큰
     * @return 사용자 ID (Long)
     * @throws CustomException 토큰이 유효하지 않은 경우
     */
    public Long getUserIdFromRefreshToken(String token) {
        try {
            return Long.parseLong(getClaims(token).getSubject());
        } catch (Exception e) {
            log.error("리프레시 토큰에서 userId 추출 실패 - message: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * 토큰으로부터 Spring Security 인증 객체를 생성합니다.
     * UserDetailsService로 사용자를 조회한 후 Authentication 객체를 생성합니다.
     *
     * @param token JWT 토큰
     * @return Authentication 객체
     */
    public Authentication getAuthentication(String token) {
        String userId = getUserId(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }
}