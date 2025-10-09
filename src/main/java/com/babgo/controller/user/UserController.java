package com.babgo.controller.user;

import com.babgo.application.user.UserFacade;
import com.babgo.controller.user.dto.UserRequest;
import com.babgo.controller.user.dto.UserResponse;
import com.babgo.global.api.ApiResponse;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import com.babgo.global.security.jwt.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserFacade userFacade;
    private final JwtProperties jwtProperties;

    // 고객 회원가입: 이메일, 비밀번호, 이름, 닉네임, 전화번호로 CUSTOMER 권한 사용자 생성 후 자동 로그인하여 액세스/리프레시 토큰 발급
    @PostMapping("/register/user")
    public ResponseEntity<ApiResponse<UserResponse.SignUpResponse>> signUpCustomer(
            @Valid @RequestBody UserRequest.CustomerSignUpRequest request) {
        log.info("고객 회원가입 요청: email={}", request.getEmail());
        UserResponse.SignUpResponse response = userFacade.signUpCustomer(request);
        // 회원가입 후 자동 로그인: 액세스/리프레시 토큰을 HttpOnly 쿠키로 전달 (Redis에도 저장)
        UserResponse.LoginResponse loginResponse = userFacade.login(
                UserRequest.LoginRequest.of(request.getEmail(), request.getPassword())
        );
        ResponseCookie accessTokenCookie = createAccessTokenCookie(loginResponse.getAccessToken());
        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(loginResponse.getRefreshToken());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Authorization", "Bearer " + loginResponse.getAccessToken())
                .header("Set-Cookie", accessTokenCookie.toString())
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(ApiResponse.success(response));
    }

    // 사장 회원가입: 이메일, 비밀번호, 이름, 닉네임, 전화번호로 OWNER 권한 사용자 생성 후 자동 로그인하여 액세스/리프레시 토큰 발급
    @PostMapping("/register/owner")
    public ResponseEntity<ApiResponse<UserResponse.SignUpResponse>> signUpOwner(
            @Valid @RequestBody UserRequest.OwnerSignUpRequest request) {
        log.info("사장 회원가입 요청: email={}", request.getEmail());
        UserResponse.SignUpResponse response = userFacade.signUpOwner(request);
        // 회원가입 후 자동 로그인: 액세스/리프레시 토큰을 HttpOnly 쿠키로 전달 (Redis에도 저장)
        UserResponse.LoginResponse loginResponse = userFacade.login(
                UserRequest.LoginRequest.of(request.getEmail(), request.getPassword())
        );
        ResponseCookie accessTokenCookie = createAccessTokenCookie(loginResponse.getAccessToken());
        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(loginResponse.getRefreshToken());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Authorization", "Bearer " + loginResponse.getAccessToken())
                .header("Set-Cookie", accessTokenCookie.toString())
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(ApiResponse.success(response));
    }


    // 고객 로그인: 액세스/리프레시 토큰을 HttpOnly 쿠키로 전달 (Redis에도 저장)
    @PostMapping("/user/login")
    public ResponseEntity<ApiResponse<String>> userLogin(
            @Valid @RequestBody UserRequest.LoginRequest request) {
        log.info("고객 로그인 요청: email={}", request.getEmail());
        UserResponse.LoginResponse response = userFacade.login(request);
        ResponseCookie accessTokenCookie = createAccessTokenCookie(response.getAccessToken());
        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(response.getRefreshToken());
        return ResponseEntity
                .ok()
                .header("Authorization", "Bearer " + response.getAccessToken())
                .header("Set-Cookie", accessTokenCookie.toString())
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(ApiResponse.success("로그인 성공"));
    }

    // 고객 로그아웃: Redis에서 리프레시 토큰 삭제 및 블랙리스트 추가, 쿠키 삭제
    @PostMapping( "/user/logout")
    public ResponseEntity<ApiResponse<String>> userLogout() {
        log.info("고객 로그아웃 요청");
        userFacade.logout();
        ResponseCookie accessTokenCookie = createExpiredCookie("accessToken");
        ResponseCookie refreshTokenCookie = createExpiredCookie("refreshToken");
        return ResponseEntity
                .ok()
                .header("Set-Cookie", accessTokenCookie.toString())
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(ApiResponse.success("로그아웃 성공"));
    }

    // 사장 로그인: 액세스/리프레시 토큰을 HttpOnly 쿠키로 전달 (Redis에도 저장)
    @PostMapping("/owner/login")
    public ResponseEntity<ApiResponse<String>> ownerLogin(
            @Valid @RequestBody UserRequest.LoginRequest request) {
        log.info("사장 로그인 요청: email={}", request.getEmail());
        UserResponse.LoginResponse response = userFacade.login(request);
        ResponseCookie accessTokenCookie = createAccessTokenCookie(response.getAccessToken());
        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(response.getRefreshToken());
        return ResponseEntity
                .ok()
                .header("Authorization", "Bearer " + response.getAccessToken())
                .header("Set-Cookie", accessTokenCookie.toString())
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(ApiResponse.success("로그인 성공"));
    }

    // 사장 로그아웃: Redis에서 리프레시 토큰 삭제 및 블랙리스트 추가, 쿠키 삭제
    @PostMapping("/owner/logout")
    public ResponseEntity<ApiResponse<String>> ownerLogout() {
        log.info("사장 로그아웃 요청");
        userFacade.logout();
        ResponseCookie accessTokenCookie = createExpiredCookie("accessToken");
        ResponseCookie refreshTokenCookie = createExpiredCookie("refreshToken");
        return ResponseEntity
                .ok()
                .header("Set-Cookie", accessTokenCookie.toString())
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(ApiResponse.success("로그아웃 성공"));
    }

    // 토큰 갱신: 쿠키에서 리프레시 토큰 추출 후 액세스 토큰만 재발급 (리프레시 토큰은 유효 기간 동안 재사용)
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<UserResponse.RefreshTokenResponse>> refresh(HttpServletRequest request) {
        log.info("토큰 갱신 요청");
        String refreshToken = resolveRefreshToken(request);
        if (refreshToken == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        UserResponse.RefreshTokenResponse response = userFacade.refreshToken(refreshToken);
        ResponseCookie accessTokenCookie = createAccessTokenCookie(response.getAccessToken());
        return ResponseEntity
                .ok()
                .header("Authorization", "Bearer " + response.getAccessToken())
                .header("Set-Cookie", accessTokenCookie.toString())
                .body(ApiResponse.success(response));
    }

    // 리프레시 토큰 추출: 쿠키 우선, 없으면 X-Refresh-Token 헤더, 마지막으로 Authorization 헤더 확인 (하위 호환성)
    private String resolveRefreshToken(HttpServletRequest request) {
        // 1. 쿠키에서 찾기
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // 2. X-Refresh-Token 헤더에서 찾기
        String refreshToken = request.getHeader("X-Refresh-Token");
        if (StringUtils.hasText(refreshToken)) {
            return refreshToken;
        }

        // 3. Authorization 헤더에서 찾기 (하위 호환성)
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    // Access Token 쿠키 생성: HttpOnly, Secure, SameSite 설정으로 XSS/CSRF 방어
    private ResponseCookie createAccessTokenCookie(String token) {
        return ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(jwtProperties.getCookie().isSecure())
                .path("/")
                .maxAge(jwtProperties.getAccessTokenExpiration() / 1000)  // 초 단위로 변환
                .sameSite(jwtProperties.getCookie().getSameSite())
                .build();
    }

    // Refresh Token 쿠키 생성: HttpOnly, Secure, SameSite 설정으로 XSS/CSRF 방어
    private ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(jwtProperties.getCookie().isSecure())
                .path("/")
                .maxAge(jwtProperties.getRefreshTokenExpiration() / 1000)  // 초 단위로 변환
                .sameSite(jwtProperties.getCookie().getSameSite())
                .build();
    }

    // 쿠키 삭제용 빈 쿠키 생성: Max-Age=0으로 즉시 만료
    private ResponseCookie createExpiredCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(jwtProperties.getCookie().isSecure())
                .path("/")
                .maxAge(0)
                .sameSite(jwtProperties.getCookie().getSameSite())
                .build();
    }
}
