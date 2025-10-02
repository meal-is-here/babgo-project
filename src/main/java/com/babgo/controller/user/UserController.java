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

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(
            @Valid @RequestBody UserRequest.LoginRequest request) {
        log.info("로그인 요청: email={}", request.getEmail());

        UserResponse.LoginResponse response = userService.login(request);

        String cookieValue = String.format(
                "accessToken=%s; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=900",
                response.getAccessToken()
        );

        return ResponseEntity
                .ok()
                .header("Set-Cookie", cookieValue)
                .body(ApiResponse.success("로그인 성공"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        log.info("로그아웃 요청");

        String cookieValue = "accessToken=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0";

        return ResponseEntity
                .ok()
                .header("Set-Cookie", cookieValue)
                .body(ApiResponse.success("로그아웃 성공"));
    }
}
