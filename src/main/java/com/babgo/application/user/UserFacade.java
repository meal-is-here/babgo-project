package com.babgo.application.user;

import com.babgo.controller.user.dto.UserRequest;
import com.babgo.controller.user.dto.UserResponse;
import com.babgo.domain.user.UserAuthService;
import com.babgo.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserFacade - Controller와 Domain Service 사이의 중간 계층
 * - DTO 변환 및 비즈니스 로직 조합
 * - 인증 관련 로직은 UserAuthService에 위임
 * - 사용자 CRUD는 UserService에 위임
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFacade {

    private final UserService userService;
    private final UserAuthService userAuthService;

    // ========== 회원가입 ==========

    /**
     * 고객 회원가입
     */
    @Transactional
    public UserResponse.SignUpResponse signUpCustomer(UserRequest.CustomerSignUpRequest request) {
        return userService.signUpCustomer(request);
    }

    /**
     * 사장 회원가입
     */
    @Transactional
    public UserResponse.SignUpResponse signUpOwner(UserRequest.OwnerSignUpRequest request) {
        return userService.signUpOwner(request);
    }

    // ========== 로그인/로그아웃 (UserAuthService에 위임) ==========

    /**
     * 로그인 (고객/사장 공통)
     */
    @Transactional(readOnly = true)
    public UserResponse.LoginResponse login(UserRequest.LoginRequest request) {
        return userAuthService.login(request);
    }

    /**
     * 멀티 디바이스 로그인
     * @param request 로그인 요청
     * @param deviceId 디바이스 식별자 (UUID, 기기 고유 ID 등)
     */
    @Transactional(readOnly = true)
    public UserResponse.LoginResponse loginWithDevice(UserRequest.LoginRequest request, String deviceId) {
        return userAuthService.loginWithDevice(request, deviceId);
    }

    /**
     * 리프레시 토큰을 통한 액세스 토큰 갱신
     */
    @Transactional(readOnly = true)
    public UserResponse.RefreshTokenResponse refreshToken(String refreshToken) {
        return userAuthService.refreshAccessToken(refreshToken);
    }

    /**
     * 로그아웃 (단일 디바이스)
     * SecurityContext에서 인증된 사용자 정보 추출 후 Redis에서 리프레시 토큰 삭제
     */
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailInfo userDetailInfo) {
            Long userId = Long.parseLong(userDetailInfo.getUserId());
            userAuthService.logout(userId);
        }
    }

    /**
     * 특정 디바이스 로그아웃
     * @param deviceId 디바이스 식별자
     */
    public void logoutDevice(String deviceId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailInfo userDetailInfo) {
            Long userId = Long.parseLong(userDetailInfo.getUserId());
            userAuthService.logoutDevice(userId, deviceId);
        }
    }

    /**
     * 모든 디바이스 강제 로그아웃 (비밀번호 변경 시 등)
     */
    public void logoutAllDevices() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailInfo userDetailInfo) {
            Long userId = Long.parseLong(userDetailInfo.getUserId());
            userAuthService.logoutAllDevices(userId);
        }
    }

    // ========== 사용자 조회 ==========

    /**
     * 현재 로그인한 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    public UserDetailInfo getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailInfo userDetailInfo) {
            return userDetailInfo;
        }
        throw new RuntimeException("인증되지 않은 사용자");
    }

    /**
     * 현재 활성 세션 수 조회
     */
    public int getActiveSessionCount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailInfo userDetailInfo) {
            Long userId = Long.parseLong(userDetailInfo.getUserId());
            return userAuthService.getActiveSessionCount(userId);
        }
        return 0;
    }
}