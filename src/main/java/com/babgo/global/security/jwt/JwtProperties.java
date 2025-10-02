package com.babgo.global.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 설정값을 application.yml에서 읽어오는 클래스
 *
 * application.yml 예시:
 * jwt:
 *   secret: your-secret-key-min-256-bits
 *   access-token-expiration: 1800000    # 30분 (밀리초)
 *   refresh-token-expiration: 604800000 # 7일 (밀리초)
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")  // application.yml의 jwt: 하위 속성들을 자동 매핑
public class JwtProperties {

    /**
     * JWT 서명에 사용할 비밀키
     * 최소 256비트(32바이트) 이상 권장
     * .env 파일에 저장: JWT_SECRET=your-secret-key
     */
    private String secret;

    /**
     * Access Token 만료 시간 (밀리초)
     * 기본값: 30분 (1800000ms)
     */
    private Long accessTokenExpiration;

    /**
     * Refresh Token 만료 시간 (밀리초)
     * 기본값: 7일 (604800000ms)
     */
    private Long refreshTokenExpiration;

    // TODO: 필요시 추가 설정
    // - private String issuer;  // 토큰 발행자
    // - private String audience; // 토큰 대상
}