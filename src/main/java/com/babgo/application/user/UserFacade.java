package com.babgo.application.user;

import com.babgo.controller.user.dto.UserRequest;
import com.babgo.controller.user.dto.UserResponse;
import com.babgo.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// UserFacade - Controller와 Domain Service 사이의 중간 계층으로 DTO 변환 및 비즈니스 로직 조합을 담당
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFacade {

    private final UserService userService;

    // 고객 회원가입
    @Transactional
    public UserResponse.SignUpResponse signUpCustomer(UserRequest.CustomerSignUpRequest request) {
        return userService.signUpCustomer(request);
    }

    // 사장 회원가입
    @Transactional
    public UserResponse.SignUpResponse signUpOwner(UserRequest.OwnerSignUpRequest request) {
        return userService.signUpOwner(request);
    }

    // 로그인 (고객/사장 공통)
    @Transactional(readOnly = true)
    public UserResponse.LoginResponse login(UserRequest.LoginRequest request) {
        return userService.login(request);
    }

    // 리프레시 토큰을 통한 액세스 토큰 갱신
    @Transactional(readOnly = true)
    public UserResponse.RefreshTokenResponse refreshToken(String refreshToken) {
        return userService.refreshToken(refreshToken);
    }

    // 로그아웃 (Redis에서 리프레시 토큰 삭제)
    public void logout() {
        // SecurityContext에서 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailInfo) {
            UserDetailInfo userDetailInfo = (UserDetailInfo) authentication.getPrincipal();
            Long userId = Long.parseLong(userDetailInfo.getUserId());
            userService.logout(userId);
        }
    }
}
