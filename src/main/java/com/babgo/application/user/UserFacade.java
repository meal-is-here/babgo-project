package com.babgo.application.user;

import com.babgo.controller.user.dto.UserRequest;
import com.babgo.controller.user.dto.UserResponse;
import com.babgo.domain.user.User;
import com.babgo.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
