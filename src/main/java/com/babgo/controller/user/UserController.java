package com.babgo.controller.user;

import com.babgo.application.user.UserFacade;
import com.babgo.controller.user.dto.UserRequest;
import com.babgo.controller.user.dto.UserResponse;
import com.babgo.global.api.ApiResponse;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserFacade userFacade;

    // 고객 회원가입: 이메일, 비밀번호, 이름, 닉네임, 전화번호로 CUSTOMER 권한 사용자 생성
    @PostMapping("/register/user")
    public ResponseEntity<ApiResponse<UserResponse.SignUpResponse>> signUpCustomer(
            @Valid @RequestBody UserRequest.CustomerSignUpRequest request) {
        log.info("고객 회원가입 요청: email={}", request.getEmail());
        UserResponse.SignUpResponse response = userFacade.signUpCustomer(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // 사장 회원가입: 이메일, 비밀번호, 이름, 닉네임, 전화번호로 OWNER 권한 사용자 생성
    @PostMapping("/register/owner")
    public ResponseEntity<ApiResponse<UserResponse.SignUpResponse>> signUpOwner(
            @Valid @RequestBody UserRequest.OwnerSignUpRequest request) {
        log.info("사장 회원가입 요청: email={}", request.getEmail());
        UserResponse.SignUpResponse response = userFacade.signUpOwner(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }


    // 고객 로그인: 액세스 토큰을 Authorization 헤더와 쿠키로, 리프레시 토큰을 X-Refresh-Token 헤더로 전달 (Redis에도 저장)
    @PostMapping("/user/login")
    public ResponseEntity<ApiResponse<String>> userLogin(
            @Valid @RequestBody UserRequest.LoginRequest request) {
        log.info("고객 로그인 요청: email={}", request.getEmail());
        UserResponse.LoginResponse response = userFacade.login(request);
        String accessTokenCookie = String.format(
                "accessToken=%s; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=900",
                response.getAccessToken()
        );
        return ResponseEntity
                .ok()
                .header("Authorization", "Bearer " + response.getAccessToken())
                .header("Set-Cookie", accessTokenCookie)
                .header("X-Refresh-Token", response.getRefreshToken())
                .body(ApiResponse.success("로그인 성공"));
    }

    // 고객 로그아웃: Redis에서 리프레시 토큰 삭제 및 블랙리스트 추가, 액세스 토큰 쿠키 삭제
    @PostMapping( "/user/logout")
    public ResponseEntity<ApiResponse<String>> userLogout() {
        log.info("고객 로그아웃 요청");
        userFacade.logout();
        String accessTokenCookie = "accessToken=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0";
        return ResponseEntity
                .ok()
                .header("Set-Cookie", accessTokenCookie)
                .body(ApiResponse.success("로그아웃 성공"));
    }

    // 사장 로그인: 액세스 토큰을 Authorization 헤더와 쿠키로, 리프레시 토큰을 X-Refresh-Token 헤더로 전달 (Redis에도 저장)
    @PostMapping("/owner/login")
    public ResponseEntity<ApiResponse<String>> ownerLogin(
            @Valid @RequestBody UserRequest.LoginRequest request) {
        log.info("사장 로그인 요청: email={}", request.getEmail());
        UserResponse.LoginResponse response = userFacade.login(request);
        String accessTokenCookie = String.format(
                "accessToken=%s; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=900",
                response.getAccessToken()
        );
        return ResponseEntity
                .ok()
                .header("Authorization", "Bearer " + response.getAccessToken())
                .header("Set-Cookie", accessTokenCookie)
                .header("X-Refresh-Token", response.getRefreshToken())
                .body(ApiResponse.success("로그인 성공"));
    }

    // 사장 로그아웃: Redis에서 리프레시 토큰 삭제 및 블랙리스트 추가, 액세스 토큰 쿠키 삭제
    @PostMapping("/owner/logout")
    public ResponseEntity<ApiResponse<String>> ownerLogout() {
        log.info("사장 로그아웃 요청");
        userFacade.logout();
        String accessTokenCookie = "accessToken=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0";
        return ResponseEntity
                .ok()
                .header("Set-Cookie", accessTokenCookie)
                .body(ApiResponse.success("로그아웃 성공"));
    }

    // 토큰 갱신: X-Refresh-Token 헤더에서 리프레시 토큰 추출 후 액세스 토큰만 재발급 (리프레시 토큰은 유효 기간 동안 재사용)
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<UserResponse.RefreshTokenResponse>> refresh(HttpServletRequest request) {
        log.info("토큰 갱신 요청");
        String refreshToken = resolveRefreshToken(request);
        if (refreshToken == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        UserResponse.RefreshTokenResponse response = userFacade.refreshToken(refreshToken);
        String accessTokenCookie = String.format(
                "accessToken=%s; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=900",
                response.getAccessToken()
        );
        return ResponseEntity
                .ok()
                .header("Authorization", "Bearer " + response.getAccessToken())
                .header("Set-Cookie", accessTokenCookie)
                .body(ApiResponse.success(response));
    }

    // 리프레시 토큰 추출: X-Refresh-Token 헤더 우선, 없으면 Authorization 헤더의 Bearer 토큰 확인 (하위 호환성)
    private String resolveRefreshToken(HttpServletRequest request) {
        String refreshToken = request.getHeader("X-Refresh-Token");
        if (StringUtils.hasText(refreshToken)) {
            return refreshToken;
        }
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
