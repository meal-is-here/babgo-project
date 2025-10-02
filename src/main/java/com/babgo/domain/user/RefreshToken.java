package com.babgo.domain.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

/**
 * Refresh Token 엔티티 (Redis 저장)
 *
 * Redis에 저장되어 Access Token 재발급에 사용됩니다.
 * TTL(Time To Live)을 통해 자동으로 만료됩니다.
 */
@Getter
@RedisHash(value = "refreshToken")  // Redis에 "refreshToken:" prefix로 저장됨
@AllArgsConstructor
@Builder
public class RefreshToken {

    /**
     * Redis Key (사용자 ID)
     * Redis에 "refreshToken:{userId}" 형태로 저장됩니다
     */
    @Id
    private String userId;

    /**
     * Refresh Token 값
     * JWT 형식의 토큰 문자열
     */
    private String token;

    /**
     * TTL (Time To Live) - 초 단위
     * 이 시간이 지나면 Redis에서 자동으로 삭제됩니다
     * JwtProperties의 refreshTokenExpiration 값을 사용합니다
     */
    @TimeToLive
    private Long expiration;

    // TODO: Token 갱신 메소드를 작성해야 합니다
    // - 기존 토큰을 새로운 토큰으로 교체
    // - expiration도 새로 설정
    public void updateToken(String newToken, Long newExpiration) {
        // 구현 필요
    }

    // TODO: Token 유효성 확인 메소드를 작성해야 합니다
    // - 토큰이 null이 아닌지 확인
    // - 필요시 추가 검증 로직 작성
    public boolean isValid() {
        // 구현 필요
        return false;
    }
}