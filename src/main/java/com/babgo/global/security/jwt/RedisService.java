package com.babgo.global.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    // 리프레시 토큰 저장: Redis에 userId를 키로 토큰을 저장하고 TTL 설정
    public void saveRefreshToken(Long userId, String refreshToken, long expirationMillis) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, expirationMillis, TimeUnit.MILLISECONDS);
        log.info("리프레시 토큰 저장: userId={}", userId);
    }

    // 리프레시 토큰 조회: userId로 Redis에서 저장된 토큰 조회
    public String getRefreshToken(Long userId) {
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
    }

    // 리프레시 토큰 삭제: 로그아웃 시 Redis에서 토큰 제거
    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        log.info("리프레시 토큰 삭제: userId={}", userId);
    }

    // 리프레시 토큰 검증: Redis 저장값과 요청 토큰 비교하여 일치 여부 확인
    public boolean validateRefreshToken(Long userId, String refreshToken) {
        String storedToken = getRefreshToken(userId);
        if (storedToken == null) {
            log.warn("리프레시 토큰 없음: userId={}", userId);
            return false;
        }
        return storedToken.equals(refreshToken);
    }

    // 블랙리스트 추가: 로그아웃된 토큰을 블랙리스트에 추가하여 재사용 방지 (원래 토큰 유효기간까지 보관)
    public void addToBlacklist(String refreshToken, long expirationMillis) {
        String key = BLACKLIST_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(key, "revoked", expirationMillis, TimeUnit.MILLISECONDS);
        log.info("토큰 블랙리스트 추가: {}", refreshToken.substring(0, Math.min(20, refreshToken.length())));
    }

    // 블랙리스트 확인: 토큰이 블랙리스트에 있는지 확인하여 재사용 감지
    public boolean isBlacklisted(String refreshToken) {
        String key = BLACKLIST_PREFIX + refreshToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}