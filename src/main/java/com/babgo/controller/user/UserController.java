package com.babgo.controller.user;

import com.babgo.domain.user.UserService;
import com.babgo.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 고객 회원가입 API
     * POST /api/auth/signup/customer
     */
    @PostMapping("/signup/customer")
    public ResponseEntity<ApiResponse<UserResponse.SignUpResponse>> signUpCustomer(
            @Valid @RequestBody UserRequest.CustomerSignUpRequest request) {
        log.info("고객 회원가입 요청: userId={}, email={}", request.getUserId(), request.getEmail());

        UserResponse.SignUpResponse response = userService.signUpCustomer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 가게 사장 회원가입 API
     * POST /api/auth/signup/owner
     */
    @PostMapping("/signup/owner")
    public ResponseEntity<ApiResponse<UserResponse.SignUpResponse>> signUpOwner(
            @Valid @RequestBody UserRequest.OwnerSignUpRequest request) {
        log.info("사장 회원가입 요청: userId={}, email={}", request.getUserId(), request.getEmail());

        UserResponse.SignUpResponse response = userService.signUpOwner(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 로그인 API
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse.LoginResponse>> login(
            @Valid @RequestBody UserRequest.LoginRequest request) {
        log.info("로그인 요청: email={}", request.getEmail());

        UserResponse.LoginResponse response = userService.login(request);

        return ResponseEntity
                .ok(ApiResponse.success(response));
    }

    // TODO: 필요한 추가 API를 작성하세요
    // - GET /api/auth/me : 현재 로그인한 사용자 정보 조회
    // - PUT /api/auth/me : 사용자 정보 수정
    // - PUT /api/auth/password : 비밀번호 변경
    // - POST /api/auth/refresh : 토큰 갱신 (Refresh Token 구현시)
    // - POST /api/auth/logout : 로그아웃 (Refresh Token 삭제)
}
