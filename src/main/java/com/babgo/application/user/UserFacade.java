package com.babgo.application.user;

import com.babgo.controller.user.UserRequest;
import com.babgo.controller.user.UserResponse;
import com.babgo.domain.user.AuthenticationService;
import com.babgo.domain.user.UserRegistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserFacade - Controller와 Domain Service 사이의 중간 계층
 * - DTO 변환 및 비즈니스 로직 조합
 * - 인증 관련 로직은 AuthenticationService에 위임
 * - 사용자 등록은 UserRegiService에 위임
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFacade {

    private final UserRegistService userRegistService;
    private final AuthenticationService authenticationService;

    // ========== 회원가입 ==========

    /**
     * 고객 회원가입
     */
    @Transactional
    public UserResponse.SignUpResponse signUpCustomer(UserRequest.CustomerSignUpRequest request) {
        return userRegistService.signUpCustomer(request);
    }

    /**
     * 사장 회원가입
     */
    @Transactional
    public UserResponse.SignUpResponse signUpOwner(UserRequest.OwnerSignUpRequest request) {
        return userRegistService.signUpOwner(request);
    }

    // ========== 로그인/로그아웃 (AuthenticationService에 위임) ==========

    /**
     * 로그인 (고객/사장 공통)
     */
    @Transactional(readOnly = true)
    public UserResponse.LoginResponse login(UserRequest.LoginRequest request) {
        return authenticationService.login(request);
    }

    /**
     * 멀티 디바이스 로그인
     * @param request 로그인 요청
     * @param deviceId 디바이스 식별자 (UUID, 기기 고유 ID 등)
     */
    @Transactional(readOnly = true)
    public UserResponse.LoginResponse loginWithDevice(UserRequest.LoginRequest request, String deviceId) {
        return authenticationService.loginWithDevice(request, deviceId);
    }

    /**
     * 리프레시 토큰을 통한 액세스 토큰 갱신
     */
    @Transactional(readOnly = true)
    public UserResponse.RefreshTokenResponse refreshToken(String refreshToken) {
        return authenticationService.refreshAccessToken(refreshToken);
    }

    /**
     * 로그아웃 (단일 디바이스)
     * SecurityContext에서 인증된 사용자 정보 추출 후 Redis에서 리프레시 토큰 삭제
     */
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailInfo userDetailInfo) {
            Long userId = Long.parseLong(userDetailInfo.getUserId());
            authenticationService.logout(userId);
        }
    }
}