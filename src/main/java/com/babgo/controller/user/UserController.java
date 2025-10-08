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
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 고객 회원가입 API
     * POST /v1/auth/register/user
     */
    @PostMapping("/register/user")
    public ResponseEntity<ApiResponse<UserResponse.SignUpResponse>> signUpCustomer(
            @Valid @RequestBody UserRequest.CustomerSignUpRequest request) {
        log.info("고객 회원가입 요청: email={}", request.getEmail());

        UserResponse.SignUpResponse response = userService.signUpCustomer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 가게 사장 회원가입 API
     * POST /v1/auth/register/owner
     */
    @PostMapping("/register/owner")
    public ResponseEntity<ApiResponse<UserResponse.SignUpResponse>> signUpOwner(
            @Valid @RequestBody UserRequest.OwnerSignUpRequest request) {
        log.info("사장 회원가입 요청: email={}", request.getEmail());

        UserResponse.SignUpResponse response = userService.signUpOwner(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 고객 로그인 API
     * POST /v1/auth/user/login
     */
    @PostMapping("/user/login")
    public ResponseEntity<ApiResponse<String>> userLogin(
            @Valid @RequestBody UserRequest.LoginRequest request) {
        log.info("고객 로그인 요청: email={}", request.getEmail());

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

    /**
     * 고객 로그아웃 API
     * POST /v1/auth/user/logout
     */
    @PostMapping( "/user/logout")
    public ResponseEntity<ApiResponse<String>> userLogout() {
        log.info("고객 로그아웃 요청");

        String cookieValue = "accessToken=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0";

        return ResponseEntity
                .ok()
                .header("Set-Cookie", cookieValue)
                .body(ApiResponse.success("로그아웃 성공"));
    }

    /**
     * 사장 로그인 API
     * POST /v1/auth/owner/login
     */
    @PostMapping("/owner/login")
    public ResponseEntity<ApiResponse<String>> ownerLogin(
            @Valid @RequestBody UserRequest.LoginRequest request) {
        log.info("사장 로그인 요청: email={}", request.getEmail());

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

    /**
     * 사장 로그아웃 API
     * POST /v1/auth/owner/logout
     */
    @PostMapping("/owner/logout")
    public ResponseEntity<ApiResponse<String>> ownerLogout() {
        log.info("사장 로그아웃 요청");

        String cookieValue = "accessToken=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0";

        return ResponseEntity
                .ok()
                .header("Set-Cookie", cookieValue)
                .body(ApiResponse.success("로그아웃 성공"));
    }
}
