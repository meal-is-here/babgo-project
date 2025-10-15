package com.babgo.controller.Auth;

import com.babgo.application.user.UserFacade;
import com.babgo.auth.jwtfilter.JwtCookiesProperties;
import com.babgo.controller.user.UserController;
import com.babgo.controller.user.UserRequest;
import com.babgo.controller.user.UserResponse;
import com.babgo.domain.user.UserRole;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@DisplayName("UserController 컨트롤러 레이어 테스트")
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserFacade userFacade;

    @MockitoBean
    private JwtCookiesProperties jwtCookiesProperties;

    private JwtCookiesProperties.Cookie cookie;

    @BeforeEach
    void setUp() {
        cookie = new JwtCookiesProperties.Cookie();
        cookie.setSecure(false);
        cookie.setSameSite("Lax");

        given(jwtCookiesProperties.getAccessTokenExpiration()).willReturn(900000L);
        given(jwtCookiesProperties.getRefreshTokenExpiration()).willReturn(86400000L);
        given(jwtCookiesProperties.getCookie()).willReturn(cookie);
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class SignUpTests {

        @Test
        @DisplayName("성공: 고객 회원가입")
        void signUpCustomer_Success() throws Exception {
            UserRequest.CustomerSignUpRequest request = new UserRequest.CustomerSignUpRequest(
                    "customer@test.com",
                    "password123",
                    "Hong Gil Dong",
                    "Customer Nickname",
                    "010-1234-5678"
            );

            UUID publicId = UUID.randomUUID();
            UserResponse.SignUpResponse signUpResponse = UserResponse.SignUpResponse.of(
                    publicId,
                    "customer@test.com",
                    "Hong Gil Dong",
                    "Customer Nickname",
                    UserRole.CUSTOMER,
                    LocalDateTime.now(),
                    "Sign up completed"
            );

            UserResponse.LoginResponse loginResponse = UserResponse.LoginResponse.of(
                    "access-token",
                    "refresh-token",
                    publicId,
                    "customer@test.com",
                    "Hong Gil Dong",
                    UserRole.CUSTOMER
            );

            given(userFacade.signUpCustomer(any(UserRequest.CustomerSignUpRequest.class)))
                    .willReturn(signUpResponse);
            given(userFacade.login(any(UserRequest.LoginRequest.class)))
                    .willReturn(loginResponse);

            mockMvc.perform(post("/v1/auth/register/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Authorization"))
                    .andExpect(header().string("Authorization", "Bearer access-token"))
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("customer@test.com"))
                    .andExpect(jsonPath("$.data.role").value("CUSTOMER"));

            verify(userFacade).signUpCustomer(any(UserRequest.CustomerSignUpRequest.class));
            verify(userFacade).login(any(UserRequest.LoginRequest.class));
        }

        @Test
        @DisplayName("성공: 사장 회원가입")
        void signUpOwner_Success() throws Exception {
            UserRequest.OwnerSignUpRequest request = new UserRequest.OwnerSignUpRequest(
                    "owner@test.com",
                    "password123",
                    "Kim Owner",
                    "Owner Nickname",
                    "010-9876-5432",
                    "123-45-67890",
                    "Delicious Restaurant"
            );

            UUID publicId = UUID.randomUUID();
            UserResponse.SignUpResponse signUpResponse = UserResponse.SignUpResponse.of(
                    publicId,
                    "owner@test.com",
                    "Kim Owner",
                    "Owner Nickname",
                    UserRole.OWNER,
                    LocalDateTime.now(),
                    "Sign up completed"
            );

            UserResponse.LoginResponse loginResponse = UserResponse.LoginResponse.of(
                    "access-token",
                    "refresh-token",
                    publicId,
                    "owner@test.com",
                    "Kim Owner",
                    UserRole.OWNER
            );

            given(userFacade.signUpOwner(any(UserRequest.OwnerSignUpRequest.class)))
                    .willReturn(signUpResponse);
            given(userFacade.login(any(UserRequest.LoginRequest.class)))
                    .willReturn(loginResponse);

            mockMvc.perform(post("/v1/auth/register/owner")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Authorization"))
                    .andExpect(header().string("Authorization", "Bearer access-token"))
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("owner@test.com"))
                    .andExpect(jsonPath("$.data.role").value("OWNER"));

            verify(userFacade).signUpOwner(any(UserRequest.OwnerSignUpRequest.class));
            verify(userFacade).login(any(UserRequest.LoginRequest.class));
        }

        @Test
        @DisplayName("실패: 유효하지 않은 이메일 형식")
        void signUpCustomer_Fail_InvalidEmail() throws Exception {
            UserRequest.CustomerSignUpRequest request = new UserRequest.CustomerSignUpRequest(
                    "invalid-email",
                    "password123",
                    "Hong Gil Dong",
                    "Nickname",
                    "010-1234-5678"
            );

            mockMvc.perform(post("/v1/auth/register/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userFacade, never()).signUpCustomer(any());
        }

        @Test
        @DisplayName("실패: 유효하지 않은 전화번호 형식")
        void signUpCustomer_Fail_InvalidPhoneNumber() throws Exception {
            UserRequest.CustomerSignUpRequest request = new UserRequest.CustomerSignUpRequest(
                    "test@test.com",
                    "password123",
                    "Hong Gil Dong",
                    "Nickname",
                    "01012345678"
            );

            mockMvc.perform(post("/v1/auth/register/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userFacade, never()).signUpCustomer(any());
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTests {

        @Test
        @DisplayName("성공: 고객 로그인")
        void userLogin_Success() throws Exception {
            UserRequest.LoginRequest request = new UserRequest.LoginRequest(
                    "customer@test.com",
                    "password123"
            );

            UUID publicId = UUID.randomUUID();
            UserResponse.LoginResponse loginResponse = UserResponse.LoginResponse.of(
                    "access-token",
                    "refresh-token",
                    publicId,
                    "customer@test.com",
                    "Hong Gil Dong",
                    UserRole.CUSTOMER
            );

            given(userFacade.login(any(UserRequest.LoginRequest.class)))
                    .willReturn(loginResponse);

            mockMvc.perform(post("/v1/auth/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Authorization"))
                    .andExpect(header().string("Authorization", "Bearer access-token"))
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value("Login successful"));

            verify(userFacade).login(any(UserRequest.LoginRequest.class));
        }

        @Test
        @DisplayName("성공: 사장 로그인")
        void ownerLogin_Success() throws Exception {
            UserRequest.LoginRequest request = new UserRequest.LoginRequest(
                    "owner@test.com",
                    "password123"
            );

            UUID publicId = UUID.randomUUID();
            UserResponse.LoginResponse loginResponse = UserResponse.LoginResponse.of(
                    "access-token",
                    "refresh-token",
                    publicId,
                    "owner@test.com",
                    "Kim Owner",
                    UserRole.OWNER
            );

            given(userFacade.login(any(UserRequest.LoginRequest.class)))
                    .willReturn(loginResponse);

            mockMvc.perform(post("/v1/auth/owner/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Authorization"))
                    .andExpect(header().string("Authorization", "Bearer access-token"))
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value("Login successful"));

            verify(userFacade).login(any(UserRequest.LoginRequest.class));
        }

        @Test
        @DisplayName("실패: 이메일 누락")
        void login_Fail_MissingEmail() throws Exception {
            String requestJson = "{\"password\":\"password123\"}";

            mockMvc.perform(post("/v1/auth/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userFacade, never()).login(any());
        }

        @Test
        @DisplayName("실패: 비밀번호 누락")
        void login_Fail_MissingPassword() throws Exception {
            String requestJson = "{\"email\":\"test@test.com\"}";

            mockMvc.perform(post("/v1/auth/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userFacade, never()).login(any());
        }

        @Test
        @DisplayName("실패: 잘못된 자격증명")
        void login_Fail_InvalidCredentials() throws Exception {
            UserRequest.LoginRequest request = new UserRequest.LoginRequest(
                    "wrong@test.com",
                    "wrongpassword"
            );

            given(userFacade.login(any(UserRequest.LoginRequest.class)))
                    .willThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));

            mockMvc.perform(post("/v1/auth/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(userFacade).login(any(UserRequest.LoginRequest.class));
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class LogoutTests {

        @Test
        @DisplayName("성공: 고객 로그아웃")
        void userLogout_Success() throws Exception {
            doNothing().when(userFacade).logout();

            mockMvc.perform(post("/v1/auth/user/logout"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value("Logout successful"));

            verify(userFacade).logout();
        }

        @Test
        @DisplayName("성공: 사장 로그아웃")
        void ownerLogout_Success() throws Exception {
            doNothing().when(userFacade).logout();

            mockMvc.perform(post("/v1/auth/owner/logout"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value("Logout successful"));

            verify(userFacade).logout();
        }
    }

    @Nested
    @DisplayName("토큰 갱신 테스트")
    class RefreshTokenTests {

        @Test
        @DisplayName("성공: 쿠키에서 리프레시 토큰으로 액세스 토큰 갱신")
        void refresh_Success_FromCookie() throws Exception {
            String refreshToken = "valid-refresh-token";
            UserResponse.RefreshTokenResponse response = UserResponse.RefreshTokenResponse.of(
                    "new-access-token",
                    null
            );

            given(userFacade.refreshToken(refreshToken)).willReturn(response);

            mockMvc.perform(post("/v1/auth/refresh")
                            .cookie(new Cookie("refreshToken", refreshToken)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Authorization"))
                    .andExpect(header().string("Authorization", "Bearer new-access-token"))
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));

            verify(userFacade).refreshToken(refreshToken);
        }

        @Test
        @DisplayName("성공: X-Refresh-Token 헤더에서 리프레시 토큰 갱신")
        void refresh_Success_FromHeader() throws Exception {
            String refreshToken = "valid-refresh-token";
            UserResponse.RefreshTokenResponse response = UserResponse.RefreshTokenResponse.of(
                    "new-access-token",
                    null
            );

            given(userFacade.refreshToken(refreshToken)).willReturn(response);

            mockMvc.perform(post("/v1/auth/refresh")
                            .header("X-Refresh-Token", refreshToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Authorization"))
                    .andExpect(header().string("Authorization", "Bearer new-access-token"))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));

            verify(userFacade).refreshToken(refreshToken);
        }

        @Test
        @DisplayName("성공: Authorization 헤더에서 리프레시 토큰 갱신 (하위 호환)")
        void refresh_Success_FromAuthorizationHeader() throws Exception {
            String refreshToken = "valid-refresh-token";
            UserResponse.RefreshTokenResponse response = UserResponse.RefreshTokenResponse.of(
                    "new-access-token",
                    null
            );

            given(userFacade.refreshToken(refreshToken)).willReturn(response);

            mockMvc.perform(post("/v1/auth/refresh")
                            .header("Authorization", "Bearer " + refreshToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Authorization"))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));

            verify(userFacade).refreshToken(refreshToken);
        }

        @Test
        @DisplayName("실패: 리프레시 토큰 누락")
        void refresh_Fail_MissingToken() throws Exception {
            mockMvc.perform(post("/v1/auth/refresh"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(userFacade, never()).refreshToken(any());
        }

        @Test
        @DisplayName("실패: 유효하지 않은 리프레시 토큰")
        void refresh_Fail_InvalidToken() throws Exception {
            String invalidToken = "invalid-refresh-token";

            given(userFacade.refreshToken(invalidToken))
                    .willThrow(new CustomException(ErrorCode.INVALID_TOKEN));

            mockMvc.perform(post("/v1/auth/refresh")
                            .cookie(new Cookie("refreshToken", invalidToken)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(userFacade).refreshToken(invalidToken);
        }

        @Test
        @DisplayName("실패: 블랙리스트에 등록된 토큰")
        void refresh_Fail_BlacklistedToken() throws Exception {
            String blacklistedToken = "blacklisted-token";

            given(userFacade.refreshToken(blacklistedToken))
                    .willThrow(new CustomException(ErrorCode.INVALID_TOKEN));

            mockMvc.perform(post("/v1/auth/refresh")
                            .cookie(new Cookie("refreshToken", blacklistedToken)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(userFacade).refreshToken(blacklistedToken);
        }
    }

    @Nested
    @DisplayName("쿠키 설정 테스트")
    class CookieTests {

        @Test
        @DisplayName("검증: 로그인 시 HttpOnly 쿠키 설정")
        void login_SetHttpOnlyCookies() throws Exception {
            UserRequest.LoginRequest request = new UserRequest.LoginRequest(
                    "test@test.com",
                    "password123"
            );

            UUID publicId = UUID.randomUUID();
            UserResponse.LoginResponse loginResponse = UserResponse.LoginResponse.of(
                    "access-token",
                    "refresh-token",
                    publicId,
                    "test@test.com",
                    "Tester",
                    UserRole.CUSTOMER
            );

            given(userFacade.login(any(UserRequest.LoginRequest.class)))
                    .willReturn(loginResponse);

            mockMvc.perform(post("/v1/auth/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(result -> {
                        String[] cookies = result.getResponse().getHeaders("Set-Cookie").toArray(new String[0]);
                        boolean hasAccessToken = false;
                        boolean hasRefreshToken = false;

                        for (String cookieValue : cookies) {
                            if (cookieValue.contains("accessToken") && cookieValue.contains("HttpOnly")) {
                                hasAccessToken = true;
                            }
                            if (cookieValue.contains("refreshToken") && cookieValue.contains("HttpOnly")) {
                                hasRefreshToken = true;
                            }
                        }

                        if (!hasAccessToken || !hasRefreshToken) {
                            throw new AssertionError("HttpOnly cookies not properly set");
                        }
                    });
        }

        @Test
        @DisplayName("검증: 로그아웃 시 쿠키 만료 (Max-Age=0)")
        void logout_ExpireCookies() throws Exception {
            doNothing().when(userFacade).logout();

            mockMvc.perform(post("/v1/auth/user/logout"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(result -> {
                        String[] cookies = result.getResponse().getHeaders("Set-Cookie").toArray(new String[0]);
                        boolean accessTokenExpired = false;
                        boolean refreshTokenExpired = false;

                        for (String cookieValue : cookies) {
                            if (cookieValue.contains("accessToken") && cookieValue.contains("Max-Age=0")) {
                                accessTokenExpired = true;
                            }
                            if (cookieValue.contains("refreshToken") && cookieValue.contains("Max-Age=0")) {
                                refreshTokenExpired = true;
                            }
                        }

                        if (!accessTokenExpired || !refreshTokenExpired) {
                            throw new AssertionError("Cookies not properly expired");
                        }
                    });
        }
    }
}