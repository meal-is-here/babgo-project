package com.babgo.domain.user.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * User 도메인 전용 Redis 서비스
 * - 인증 토큰 관리 (리프레시 토큰, 블랙리스트)
 * - 멀티 디바이스 세션 관리
 * - 동시 접속 제한
 */
@Slf4j
@Service
public class UserRedisTemplete {

    private final RedisTemplate<String, String> redisTemplate;

    public UserRedisTemplete(@Qualifier("authRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Redis Key Prefix 정의
    private static final String REFRESH_TOKEN_PREFIX = "user:refresh:";
    private static final String BLACKLIST_PREFIX = "user:blacklist:";
    private static final String SESSION_PREFIX = "user:session:";
    private static final String LOGIN_ATTEMPT_PREFIX = "user:login_attempt:";

    // ========== 리프레시 토큰 관리 ==========

    /**
     * 리프레시 토큰 저장 (단일 디바이스용)
     * @param userId 사용자 ID
     * @param refreshToken 리프레시 토큰
     * @param expirationMillis 만료 시간 (밀리초)
     */
    public void saveRefreshToken(Long userId, String refreshToken, long expirationMillis) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, expirationMillis, TimeUnit.MILLISECONDS);
        log.info("리프레시 토큰 저장: userId={}", userId);
    }

    /**
     * 리프레시 토큰 조회
     * @param userId 사용자 ID
     * @return 저장된 리프레시 토큰 (없으면 null)
     */
    public String getRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 리프레시 토큰 삭제 (로그아웃 시)
     * @param userId 사용자 ID
     */
    public void deleteRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
        log.info("리프레시 토큰 삭제: userId={}", userId);
    }

    /**
     * 리프레시 토큰 검증 (Redis 저장값과 비교)
     * @param userId 사용자 ID
     * @param refreshToken 검증할 토큰
     * @return 일치 여부
     */
    public boolean validateRefreshToken(Long userId, String refreshToken) {
        String storedToken = getRefreshToken(userId);
        if (storedToken == null) {
            log.warn("리프레시 토큰 없음: userId={}", userId);
            return false;
        }
        return storedToken.equals(refreshToken);
    }

    // ========== 블랙리스트 관리 ==========

    /**
     * 블랙리스트에 토큰 추가 (로그아웃된 토큰 재사용 방지)
     * @param refreshToken 무효화할 토큰
     * @param expirationMillis 원래 토큰의 남은 유효기간
     */
    public void addToBlacklist(String refreshToken, long expirationMillis) {
        String key = BLACKLIST_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(key, "revoked", expirationMillis, TimeUnit.MILLISECONDS);
        log.info("토큰 블랙리스트 추가: token={}", maskToken(refreshToken));
    }

    /**
     * 블랙리스트 확인
     * @param refreshToken 확인할 토큰
     * @return 블랙리스트 포함 여부
     */
    public boolean isBlacklisted(String refreshToken) {
        String key = BLACKLIST_PREFIX + refreshToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // ========== 멀티 디바이스 세션 관리 ==========

    /**
     * 디바이스별 세션 저장 (멀티 디바이스 지원)
     * @param userId 사용자 ID
     * @param deviceId 디바이스 식별자 (UUID 또는 기기 고유 ID)
     * @param refreshToken 리프레시 토큰
     * @param expiration 만료 시간
     */
    public void saveDeviceSession(Long userId, String deviceId, String refreshToken, Duration expiration) {
        String key = SESSION_PREFIX + userId + ":" + deviceId;
        redisTemplate.opsForValue().set(key, refreshToken, expiration);
        log.info("디바이스 세션 저장: userId={}, deviceId={}", userId, maskDeviceId(deviceId));
    }

    /**
     * 디바이스 세션 조회
     * @param userId 사용자 ID
     * @param deviceId 디바이스 식별자
     * @return 저장된 리프레시 토큰
     */
    public String getDeviceSession(Long userId, String deviceId) {
        String key = SESSION_PREFIX + userId + ":" + deviceId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 특정 디바이스 로그아웃
     * @param userId 사용자 ID
     * @param deviceId 디바이스 식별자
     */
    public void logoutDevice(Long userId, String deviceId) {
        String key = SESSION_PREFIX + userId + ":" + deviceId;
        redisTemplate.delete(key);
        log.info("디바이스 로그아웃: userId={}, deviceId={}", userId, maskDeviceId(deviceId));
    }

    /**
     * 모든 디바이스 로그아웃 (강제 로그아웃 또는 비밀번호 변경 시)
     * @param userId 사용자 ID
     */
    public void logoutAllDevices(Long userId) {
        String pattern = SESSION_PREFIX + userId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("모든 디바이스 로그아웃: userId={}, deviceCount={}", userId, keys.size());
        }
    }

    /**
     * 현재 접속 중인 디바이스 수 조회
     * @param userId 사용자 ID
     * @return 활성 세션 수
     */
    public int getActiveSessionCount(Long userId) {
        String pattern = SESSION_PREFIX + userId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        return keys != null ? keys.size() : 0;
    }

    /**
     * 새 세션 추가 가능 여부 확인 (동시 접속 제한)
     * @param userId 사용자 ID
     * @param maxDevices 최대 허용 디바이스 수
     * @return 추가 가능 여부
     */
    public boolean canAddNewSession(Long userId, int maxDevices) {
        int currentSessions = getActiveSessionCount(userId);
        boolean canAdd = currentSessions < maxDevices;
        if (!canAdd) {
            log.warn("동시 접속 제한 초과: userId={}, current={}, max={}", userId, currentSessions, maxDevices);
        }
        return canAdd;
    }

    // ========== 로그인 시도 제한 (Brute Force 방지) ==========

    /**
     * 로그인 실패 횟수 증가
     * @param email 이메일
     * @return 현재 실패 횟수
     */
    public Long incrementLoginAttempts(String email) {
        String key = LOGIN_ATTEMPT_PREFIX + email;
        Long attempts = redisTemplate.opsForValue().increment(key);
        // 5분 동안 유지
        redisTemplate.expire(key, 5, TimeUnit.MINUTES);
        log.debug("로그인 실패 횟수 증가: email={}, attempts={}", maskEmail(email), attempts);
        return attempts != null ? attempts : 0L;
    }

    /**
     * 로그인 실패 횟수 조회
     * @param email 이메일
     * @return 실패 횟수
     */
    public int getLoginAttempts(String email) {
        String key = LOGIN_ATTEMPT_PREFIX + email;
        String attempts = redisTemplate.opsForValue().get(key);
        return attempts != null ? Integer.parseInt(attempts) : 0;
    }

    /**
     * 로그인 실패 횟수 초기화 (성공 시)
     * @param email 이메일
     */
    public void resetLoginAttempts(String email) {
        String key = LOGIN_ATTEMPT_PREFIX + email;
        redisTemplate.delete(key);
        log.debug("로그인 실패 횟수 초기화: email={}", maskEmail(email));
    }

    /**
     * 로그인 시도 제한 확인
     * @param email 이메일
     * @param maxAttempts 최대 허용 횟수
     * @return 차단 여부
     */
    public boolean isLoginBlocked(String email, int maxAttempts) {
        return getLoginAttempts(email) >= maxAttempts;
    }

    // ========== 유틸리티 메서드 ==========

    /**
     * 토큰 마스킹 (로그용)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 20) {
            return "***";
        }
        return token.substring(0, 10) + "..." + token.substring(token.length() - 10);
    }

    /**
     * 디바이스 ID 마스킹 (로그용)
     */
    private String maskDeviceId(String deviceId) {
        if (deviceId == null || deviceId.length() < 8) {
            return "***";
        }
        return deviceId.substring(0, 4) + "***" + deviceId.substring(deviceId.length() - 4);
    }

    /**
     * 이메일 마스킹 (로그용)
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String maskedLocal = localPart.length() > 2
            ? localPart.substring(0, 2) + "***"
            : "***";
        return maskedLocal + "@" + parts[1];
    }
}