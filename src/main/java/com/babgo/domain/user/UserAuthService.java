package com.babgo.domain.user;

import com.babgo.auth.JwtTokenProvider;
import com.babgo.auth.jwtfilter.JwtCookiesProperties;
import com.babgo.controller.user.UserRequest;
import com.babgo.controller.user.UserResponse;
import com.babgo.domain.user.infrastructure.UserRedisTemplete;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import com.babgo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User 도메인 인증 서비스
 * - 로그인/로그아웃 처리
 * - 토큰 생성 및 갱신
 * - Redis를 통한 세션 관리
 * - 로그인 시도 제한
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRedisTemplete userRedisTemplete;
    private final JwtCookiesProperties jwtCookiesProperties;

    // 로그인 시도 제한 설정
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int MAX_DEVICES = 5;

    /**
     * 로그인 처리
     * @param request 로그인 요청 (이메일, 비밀번호)
     * @return 액세스 토큰 및 리프레시 토큰
     */
    @Transactional(readOnly = true)
    public UserResponse.LoginResponse login(UserRequest.LoginRequest request) {
        String email = request.getEmail();

        // 1. 로그인 시도 제한 확인 (Brute Force 방어)
        if (userRedisTemplete.isLoginBlocked(email, MAX_LOGIN_ATTEMPTS)) {
            log.warn("로그인 시도 횟수 초과: email={}", email);
            throw new CustomException(ErrorCode.TOO_MANY_ATTEMPTS);
        }

        // 2. 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    userRedisTemplete.incrementLoginAttempts(email);
                    return new CustomException(ErrorCode.INVALID_CREDENTIALS);
                });

        // 3. 삭제된 사용자 확인
        if (Boolean.TRUE.equals(user.getIsUserDeleted())) {
            throw new CustomException(ErrorCode.USER_DELETED);
        }

        // 4. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            userRedisTemplete.incrementLoginAttempts(email);
            log.warn("비밀번호 불일치: userId={}", user.getUserId());
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 5. 로그인 성공 - 실패 횟수 초기화
        userRedisTemplete.resetLoginAttempts(email);

        // 6. 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        // 7. Redis에 리프레시 토큰 저장
        userRedisTemplete.saveRefreshToken(
                user.getUserId(),
                refreshToken,
                jwtCookiesProperties.getRefreshTokenExpiration()
        );

        log.info("로그인 성공: userId={}, email={}, role={}", user.getUserId(), user.getEmail(), user.getRole());

        return UserResponse.LoginResponse.of(
                accessToken,
                refreshToken,
                user.getPublicId(),
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
    }

    /**
     * 멀티 디바이스 로그인 처리
     * @param request 로그인 요청
     * @param deviceId 디바이스 식별자
     * @return 액세스 토큰 및 리프레시 토큰
     */
    @Transactional(readOnly = true)
    public UserResponse.LoginResponse loginWithDevice(UserRequest.LoginRequest request, String deviceId) {
        String email = request.getEmail();

        // 1. 로그인 시도 제한 확인
        if (userRedisTemplete.isLoginBlocked(email, MAX_LOGIN_ATTEMPTS)) {
            log.warn("로그인 시도 횟수 초과: email={}", email);
            throw new CustomException(ErrorCode.TOO_MANY_ATTEMPTS);
        }

        // 2. 사용자 조회 및 검증
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    userRedisTemplete.incrementLoginAttempts(email);
                    return new CustomException(ErrorCode.INVALID_CREDENTIALS);
                });

        if (Boolean.TRUE.equals(user.getIsUserDeleted())) {
            throw new CustomException(ErrorCode.USER_DELETED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            userRedisTemplete.incrementLoginAttempts(email);
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 3. 동시 접속 디바이스 수 제한 확인
        if (!userRedisTemplete.canAddNewSession(user.getUserId(), MAX_DEVICES)) {
            log.warn("디바이스 수 제한 초과: userId={}", user.getUserId());
            throw new CustomException(ErrorCode.TOO_MANY_SESSIONS);
        }

        // 4. 로그인 성공 처리
        userRedisTemplete.resetLoginAttempts(email);

        // 5. 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        // 6. 디바이스별 세션 저장
        userRedisTemplete.saveDeviceSession(
                user.getUserId(),
                deviceId,
                refreshToken,
                java.time.Duration.ofMillis(jwtCookiesProperties.getRefreshTokenExpiration())
        );

        log.info("멀티 디바이스 로그인 성공: userId={}, deviceId={}", user.getUserId(), deviceId);

        return UserResponse.LoginResponse.of(
                accessToken,
                refreshToken,
                user.getPublicId(),
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
    }

    /**
     * 리프레시 토큰으로 액세스 토큰 갱신
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰
     */
    @Transactional(readOnly = true)
    public UserResponse.RefreshTokenResponse refreshAccessToken(String refreshToken) {
        // 1. 블랙리스트 확인
        if (userRedisTemplete.isBlacklisted(refreshToken)) {
            log.warn("블랙리스트 토큰 사용 시도");
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 2. JWT 토큰 자체 검증 (서명, 만료시간)
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            log.warn("유효하지 않은 리프레시 토큰");
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 3. 토큰에서 userId 추출
        Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);

        // 4. Redis 저장값과 비교 검증
        if (!userRedisTemplete.validateRefreshToken(userId, refreshToken)) {
            log.warn("Redis 저장값 불일치: userId={}", userId);
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 5. 사용자 조회
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 6. 새로운 액세스 토큰 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole()
        );

        log.info("액세스 토큰 갱신 완료: userId={}", userId);

        return UserResponse.RefreshTokenResponse.of(newAccessToken, null);
    }

    /**
     * 로그아웃 처리 (단일 디바이스)
     * @param userId 사용자 ID
     */
    public void logout(Long userId) {
        // 1. Redis에서 리프레시 토큰 조회
        String refreshToken = userRedisTemplete.getRefreshToken(userId);

        // 2. 블랙리스트에 추가 (토큰 재사용 방지)
        if (refreshToken != null) {
            userRedisTemplete.addToBlacklist(
                    refreshToken,
                    jwtCookiesProperties.getRefreshTokenExpiration()
            );
        }

        // 3. Redis에서 토큰 삭제
        userRedisTemplete.deleteRefreshToken(userId);

        log.info("로그아웃 완료: userId={}", userId);
    }

    /**
     * 특정 디바이스 로그아웃
     * @param userId 사용자 ID
     * @param deviceId 디바이스 식별자
     */
    public void logoutDevice(Long userId, String deviceId) {
        // 1. 디바이스 세션에서 토큰 조회
        String refreshToken = userRedisTemplete.getDeviceSession(userId, deviceId);

        // 2. 블랙리스트에 추가
        if (refreshToken != null) {
            userRedisTemplete.addToBlacklist(
                    refreshToken,
                    jwtCookiesProperties.getRefreshTokenExpiration()
            );
        }

        // 3. 디바이스 세션 삭제
        userRedisTemplete.logoutDevice(userId, deviceId);

        log.info("디바이스 로그아웃 완료: userId={}, deviceId={}", userId, deviceId);
    }

    /**
     * 모든 디바이스 강제 로그아웃 (비밀번호 변경, 보안 이슈 등)
     * @param userId 사용자 ID
     */
    public void logoutAllDevices(Long userId) {
        // 모든 디바이스 세션 삭제
        userRedisTemplete.logoutAllDevices(userId);

        // 기존 단일 토큰도 삭제
        String refreshToken = userRedisTemplete.getRefreshToken(userId);
        if (refreshToken != null) {
            userRedisTemplete.addToBlacklist(
                    refreshToken,
                    jwtCookiesProperties.getRefreshTokenExpiration()
            );
            userRedisTemplete.deleteRefreshToken(userId);
        }

        log.info("모든 디바이스 강제 로그아웃: userId={}", userId);
    }

    /**
     * 현재 활성 세션 수 조회
     * @param userId 사용자 ID
     * @return 활성 세션 수
     */
    public int getActiveSessionCount(Long userId) {
        return userRedisTemplete.getActiveSessionCount(userId);
    }

    /**
     * 로그인 시도 횟수 조회 (관리 목적)
     * @param email 이메일
     * @return 현재 실패 횟수
     */
    public int getLoginAttempts(String email) {
        return userRedisTemplete.getLoginAttempts(email);
    }
}