package com.babgo.auth;

import com.babgo.auth.jwtfilter.JwtCookiesProperties;
import com.babgo.domain.user.UserRole;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtCookiesProperties jwtCookiesProperties;
    private final UserDetailsService userDetailsService;
    private SecretKey secretKey;

    // JWT Secret Key 초기화: application.yml의 jwt.secret을 바탕으로 HMAC-SHA 키 생성
    @PostConstruct
    public void init() {
        byte[] keyBytes = jwtCookiesProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT Secret Key 초기화 완료");
    }

    // 액세스 토큰 생성: userId(subject), email/role(claims) 포함, 15분 유효기간
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

    // 리프레시 토큰 생성: userId(subject), type=refresh(claim) 포함, 1일 유효기간
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

    // 토큰에서 userId 추출: subject 필드 파싱
    public String getUserId(String token) {
        return getClaims(token).getSubject();
    }

    // 토큰 파싱: Secret Key로 서명 검증 후 Claims 추출
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 액세스 토큰 유효성 검증: 서명, 만료시간, 형식 등 확인
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("만료된 토큰: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 토큰: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("잘못된 형식: {}", e.getMessage());
        } catch (SecurityException e) {
            log.error("서명 검증 실패: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("빈 토큰: {}", e.getMessage());
        }
        return false;
    }

    // 리프레시 토큰 유효성 검증: type=refresh 확인 + 서명/만료시간 검증
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
            if (!"refresh".equals(claims.get("type", String.class))) {
                log.error("리프레시 토큰이 아님");
                return false;
            }
            return true;
        } catch (ExpiredJwtException e) {
            log.error("만료된 리프레시 토큰: {}", e.getMessage());
        } catch (Exception e) {
            log.error("리프레시 토큰 검증 실패: {}", e.getMessage());
        }
        return false;
    }

    // 리프레시 토큰에서 userId 추출: subject를 Long으로 파싱
    public Long getUserIdFromRefreshToken(String token) {
        try {
            return Long.parseLong(getClaims(token).getSubject());
        } catch (Exception e) {
            log.error("리프레시 토큰에서 userId 추출 실패: {}", e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰");
        }
    }

    // 토큰으로 Spring Security 인증 객체 생성: UserDetailsService로 사용자 조회 후 Authentication 생성
    public Authentication getAuthentication(String token) {
        String userId = getUserId(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }
}