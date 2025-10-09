package com.babgo.global.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// JWT 설정 정보를 담는 클래스
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")  // application.yml의 jwt: 하위 속성들을 자동 매핑
public class JwtProperties {
    private String secret;
    private Long accessTokenExpiration;
    private Long refreshTokenExpiration;
    private Cookie cookie = new Cookie();  // 쿠키 관련 설정

    @Getter
    @Setter
    public static class Cookie {
        private boolean secure = false;  // HTTPS 전용 여부
        private String domain = "localhost";  // 쿠키 도메인
        private String sameSite = "Lax";  // SameSite 정책 (Strict, Lax, None)
    }
}