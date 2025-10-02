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

    //Bean 초기화 후 SecretKey를 생성하는 메소드
    @PostConstruct
    public void init() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT Secret Key init complete");
    }

    // Access Token을 생성하는 메소드
    public String generateAccessToken(String userId, String email, UserRole role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());
        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("role", role.getKey())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(this.secretKey)
                .compact();
    }

    //Refresh Token을 생성하는 메소드
    //public String generateRefreshToken(String userId) {
    //    Date now = new Date();
    //    Date expiryDate = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration());
//
    //     return Jwts.builder()
      //          .subject(userId)
          //      .issuedAt(now)
           //     .expiration(expiryDate)
             //   .signWith(this.secretKey)
               // .compact();
    //}
    /**
     * Token에서 사용자 ID를 추출하는 메소드
     * - JWT의 subject에서 userId를 가져옵니다
     */
    public String getUserId(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Token에서 이메일을 추출하는 메소드
     * - JWT의 claims에서 email을 가져옵니다
     */
    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    /**
     * Token에서 권한을 추출하는 메소드
     * - JWT의 claims에서 role을 가져옵니다
     */
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    /**
     * Token에서 Claims를 추출하는 private 메소드
     * - JWT를 파싱하여 payload 정보를 가져옵니다
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Token 유효성을 검증하는 메소드
     * - 토큰이 유효하면 true, 아니면 false 반환
     * - 만료, 변조, 형식 오류 등을 체크합니다
     */
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

    /**
     * Token으로부터 Authentication 객체를 생성하는 메소드
     * - Spring Security의 SecurityContext에 저장될 인증 객체입니다
     * - 토큰에서 사용자 정보와 권한을 추출하여 생성합니다
     */
    public Authentication getAuthentication(String token) {
        String userId = getUserId(token);
        String email = getEmail(token);
        String role = getRole(token);

        // 권한 객체 생성
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority(role)
        );

        // UserDetails 객체 생성
        UserDetails principal = User.builder()
                .username(userId)
                .password("")  // 토큰 기반 인증에서는 비밀번호 불필요
                .authorities(authorities)
                .build();

        // Authentication 객체 생성 및 반환
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * Token의 남은 유효시간을 밀리초로 반환하는 메소드
     * - Redis TTL 설정시 사용됩니다 (초 단위로 변환 필요)
     */
    public Long getTokenExpirationTime(String token) {
        Date expiration = getClaims(token).getExpiration();
        Date now = new Date();
        return expiration.getTime() - now.getTime();
    }
}