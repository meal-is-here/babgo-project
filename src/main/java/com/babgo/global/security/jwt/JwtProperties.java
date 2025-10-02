package com.babgo.global.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")  // application.yml의 jwt: 하위 속성들을 자동 매핑
public class JwtProperties {
    private String secret;
    private Long accessTokenExpiration;
    private Long refreshTokenExpiration;

    // TODO: 필요시 추가 설정
    // - private String issuer;  // 토큰 발행자
    // - private String audience; // 토큰 대상
}