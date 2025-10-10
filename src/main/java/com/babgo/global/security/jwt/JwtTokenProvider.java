package com.babgo.global.security.jwt;

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

 //JWT 토큰 생성 및 검증 클래스
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final UserDetailsService userDetailsService;
    private SecretKey secretKey;

    // JWT 비밀키 초기화
    @PostConstruct
    public void init() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT Secret Key init complete");
    }

    // 액세스 토큰 생성
    public String generateAccessToken(Long userId, String email, UserRole role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role.getKey())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(this.secretKey)
                .compact();
    }

    // 토큰에서 사용자 ID 추출
    public String getUserId(String token) {
        return getClaims(token).getSubject();
    }

    // 토큰에서 이메일 추출
    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    // 토큰에서 권한 추출
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // 토큰에서 Claims 파싱
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("잘못된 형식의 JWT 토큰입니다: {}", e.getMessage());
        } catch (SecurityException e) {
            log.error("JWT 서명이 유효하지 않습니다: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 비어있습니다: {}", e.getMessage());
        }
        return false;
    }

    // 토큰으로 인증 객체 생성
    public Authentication getAuthentication(String token) {
        String userId = getUserId(token);

        // UserDetailsService를 통해 실제 UserDetailInfo 객체를 로드
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }
}