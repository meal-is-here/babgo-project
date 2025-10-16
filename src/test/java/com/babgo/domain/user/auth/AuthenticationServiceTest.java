package com.babgo.domain.user.auth;

import com.babgo.auth.JwtTokenProvider;
import com.babgo.auth.jwtfilter.JwtCookiesProperties;
import com.babgo.controller.user.UserRequest;
import com.babgo.controller.user.UserResponse;
import com.babgo.domain.user.User;
import com.babgo.domain.user.AuthenticationService;
import com.babgo.domain.user.infrastructure.UserRedisTemplete;
import com.babgo.domain.user.UserRole;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import com.babgo.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService 단위 테스트")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRedisTemplete userRedisTemplete;

    @Mock
    private JwtCookiesProperties jwtCookiesProperties;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private UserRequest.LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.ofCustomer(
                "test@example.com",
                "encodedPassword",
                "테스트사용자",
                "테스터",
                "010-1234-5678"
        );
        // Reflection으로 userId 설정
        try {
            var field = User.class.getDeclaredField("userId");
            field.setAccessible(true);
            field.set(testUser, 1L);
        } catch (Exception e) {
            // Ignore for test
        }

        loginRequest = UserRequest.LoginRequest.of("test@example.com", "password123");
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTests {

        @Test
        @DisplayName("성공: 정상적인 로그인")
        void login_Success() {
            // given
            given(userRedisTemplete.isLoginBlocked(anyString(), anyInt())).willReturn(false);
            given(userRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).willReturn(true);
            given(jwtTokenProvider.generateAccessToken(anyLong(), anyString(), any(UserRole.class)))
                    .willReturn("access-token");
            given(jwtTokenProvider.generateRefreshToken(anyLong())).willReturn("refresh-token");
            given(jwtCookiesProperties.getRefreshTokenExpiration()).willReturn(86400000L);

            // when
            UserResponse.LoginResponse response = authenticationService.login(loginRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
            assertThat(response.getRole()).isEqualTo(testUser.getRole());

            verify(userRedisTemplete).resetLoginAttempts(loginRequest.getEmail());
            verify(userRedisTemplete).saveRefreshToken(
                    eq(testUser.getUserId()),
                    eq("refresh-token"),
                    eq(86400000L)
            );
        }

        @Test
        @DisplayName("실패: 로그인 시도 횟수 초과 (Brute Force 방어)")
        void login_Fail_TooManyAttempts() {
            // given
            given(userRedisTemplete.isLoginBlocked(loginRequest.getEmail(), 5)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authenticationService.login(loginRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOO_MANY_ATTEMPTS);

            verify(userRepository, never()).findByEmail(anyString());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자")
        void login_Fail_UserNotFound() {
            // given
            given(userRedisTemplete.isLoginBlocked(anyString(), anyInt())).willReturn(false);
            given(userRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authenticationService.login(loginRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);

            verify(userRedisTemplete).incrementLoginAttempts(loginRequest.getEmail());
        }

        @Test
        @DisplayName("실패: 삭제된 사용자")
        void login_Fail_UserDeleted() {
            // given
            testUser.markAsDeleted();
            given(userRedisTemplete.isLoginBlocked(anyString(), anyInt())).willReturn(false);
            given(userRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> authenticationService.login(loginRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_DELETED);
        }

        @Test
        @DisplayName("실패: 비밀번호 불일치")
        void login_Fail_InvalidPassword() {
            // given
            given(userRedisTemplete.isLoginBlocked(anyString(), anyInt())).willReturn(false);
            given(userRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authenticationService.login(loginRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);

            verify(userRedisTemplete).incrementLoginAttempts(loginRequest.getEmail());
        }
    }

    @Nested
    @DisplayName("멀티 디바이스 로그인 테스트")
    class MultiDeviceLoginTests {

        @Test
        @DisplayName("성공: 정상적인 멀티 디바이스 로그인")
        void loginWithDevice_Success() {
            // given
            String deviceId = "device-uuid-123";
            given(userRedisTemplete.isLoginBlocked(anyString(), anyInt())).willReturn(false);
            given(userRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).willReturn(true);
            given(userRedisTemplete.canAddNewSession(testUser.getUserId(), 5)).willReturn(true);
            given(jwtTokenProvider.generateAccessToken(anyLong(), anyString(), any(UserRole.class)))
                    .willReturn("access-token");
            given(jwtTokenProvider.generateRefreshToken(anyLong())).willReturn("refresh-token");
            given(jwtCookiesProperties.getRefreshTokenExpiration()).willReturn(86400000L);

            // when
            UserResponse.LoginResponse response = authenticationService.loginWithDevice(loginRequest, deviceId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token");

            verify(userRedisTemplete).saveDeviceSession(
                    eq(testUser.getUserId()),
                    eq(deviceId),
                    eq("refresh-token"),
                    any()
            );
        }

        @Test
        @DisplayName("실패: 디바이스 수 제한 초과")
        void loginWithDevice_Fail_TooManySessions() {
            // given
            String deviceId = "device-uuid-123";
            given(userRedisTemplete.isLoginBlocked(anyString(), anyInt())).willReturn(false);
            given(userRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).willReturn(true);
            given(userRedisTemplete.canAddNewSession(testUser.getUserId(), 5)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authenticationService.loginWithDevice(loginRequest, deviceId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOO_MANY_SESSIONS);

            verify(userRedisTemplete, never()).saveDeviceSession(anyLong(), anyString(), anyString(), any());
        }
    }

    @Nested
    @DisplayName("토큰 갱신 테스트")
    class RefreshTokenTests {

        @Test
        @DisplayName("성공: 리프레시 토큰으로 액세스 토큰 갱신")
        void refreshAccessToken_Success() {
            // given
            String refreshToken = "valid-refresh-token";
            given(userRedisTemplete.isBlacklisted(refreshToken)).willReturn(false);
            given(jwtTokenProvider.validateRefreshToken(refreshToken)).willReturn(true);
            given(jwtTokenProvider.getUserIdFromRefreshToken(refreshToken)).willReturn(1L);
            given(userRedisTemplete.validateRefreshToken(1L, refreshToken)).willReturn(true);
            given(userRepository.findByUserIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testUser));
            given(jwtTokenProvider.generateAccessToken(anyLong(), anyString(), any(UserRole.class)))
                    .willReturn("new-access-token");

            // when
            UserResponse.RefreshTokenResponse response = authenticationService.refreshAccessToken(refreshToken);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        }

        @Test
        @DisplayName("실패: 블랙리스트에 등록된 토큰")
        void refreshAccessToken_Fail_Blacklisted() {
            // given
            String refreshToken = "blacklisted-token";
            given(userRedisTemplete.isBlacklisted(refreshToken)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authenticationService.refreshAccessToken(refreshToken))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);
        }

        @Test
        @DisplayName("실패: 유효하지 않은 토큰")
        void refreshAccessToken_Fail_InvalidToken() {
            // given
            String refreshToken = "invalid-token";
            given(userRedisTemplete.isBlacklisted(refreshToken)).willReturn(false);
            given(jwtTokenProvider.validateRefreshToken(refreshToken)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authenticationService.refreshAccessToken(refreshToken))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);
        }

        @Test
        @DisplayName("실패: Redis 저장값과 불일치")
        void refreshAccessToken_Fail_RedisMismatch() {
            // given
            String refreshToken = "valid-but-different-token";
            given(userRedisTemplete.isBlacklisted(refreshToken)).willReturn(false);
            given(jwtTokenProvider.validateRefreshToken(refreshToken)).willReturn(true);
            given(jwtTokenProvider.getUserIdFromRefreshToken(refreshToken)).willReturn(1L);
            given(userRedisTemplete.validateRefreshToken(1L, refreshToken)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authenticationService.refreshAccessToken(refreshToken))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);
        }

        @Test
        @DisplayName("실패: 사용자를 찾을 수 없음")
        void refreshAccessToken_Fail_UserNotFound() {
            // given
            String refreshToken = "valid-refresh-token";
            given(userRedisTemplete.isBlacklisted(refreshToken)).willReturn(false);
            given(jwtTokenProvider.validateRefreshToken(refreshToken)).willReturn(true);
            given(jwtTokenProvider.getUserIdFromRefreshToken(refreshToken)).willReturn(1L);
            given(userRedisTemplete.validateRefreshToken(1L, refreshToken)).willReturn(true);
            given(userRepository.findByUserIdAndDeletedAtIsNull(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authenticationService.refreshAccessToken(refreshToken))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class LogoutTests {

        @Test
        @DisplayName("성공: 단일 디바이스 로그아웃")
        void logout_Success() {
            // given
            Long userId = 1L;
            String refreshToken = "refresh-token";
            given(userRedisTemplete.getRefreshToken(userId)).willReturn(refreshToken);
            given(jwtCookiesProperties.getRefreshTokenExpiration()).willReturn(86400000L);

            // when
            authenticationService.logout(userId);

            // then
            verify(userRedisTemplete).addToBlacklist(eq(refreshToken), eq(86400000L));
            verify(userRedisTemplete).deleteRefreshToken(userId);
        }

        @Test
        @DisplayName("성공: 토큰이 없어도 로그아웃 처리")
        void logout_Success_NoToken() {
            // given
            Long userId = 1L;
            given(userRedisTemplete.getRefreshToken(userId)).willReturn(null);

            // when
            authenticationService.logout(userId);

            // then
            verify(userRedisTemplete, never()).addToBlacklist(anyString(), anyLong());
            verify(userRedisTemplete).deleteRefreshToken(userId);
        }

        @Test
        @DisplayName("성공: 특정 디바이스 로그아웃")
        void logoutDevice_Success() {
            // given
            Long userId = 1L;
            String deviceId = "device-123";
            String refreshToken = "device-refresh-token";
            given(userRedisTemplete.getDeviceSession(userId, deviceId)).willReturn(refreshToken);
            given(jwtCookiesProperties.getRefreshTokenExpiration()).willReturn(86400000L);

            // when
            authenticationService.logoutDevice(userId, deviceId);

            // then
            verify(userRedisTemplete).addToBlacklist(eq(refreshToken), eq(86400000L));
            verify(userRedisTemplete).logoutDevice(userId, deviceId);
        }

        @Test
        @DisplayName("성공: 모든 디바이스 강제 로그아웃")
        void logoutAllDevices_Success() {
            // given
            Long userId = 1L;
            String refreshToken = "refresh-token";
            given(userRedisTemplete.getRefreshToken(userId)).willReturn(refreshToken);
            given(jwtCookiesProperties.getRefreshTokenExpiration()).willReturn(86400000L);

            // when
            authenticationService.logoutAllDevices(userId);

            // then
            verify(userRedisTemplete).logoutAllDevices(userId);
            verify(userRedisTemplete).addToBlacklist(eq(refreshToken), eq(86400000L));
            verify(userRedisTemplete).deleteRefreshToken(userId);
        }
    }

    @Nested
    @DisplayName("세션 관리 테스트")
    class SessionManagementTests {

        @Test
        @DisplayName("성공: 활성 세션 수 조회")
        void getActiveSessionCount_Success() {
            // given
            Long userId = 1L;
            given(userRedisTemplete.getActiveSessionCount(userId)).willReturn(3);

            // when
            int count = authenticationService.getActiveSessionCount(userId);

            // then
            assertThat(count).isEqualTo(3);
            verify(userRedisTemplete).getActiveSessionCount(userId);
        }

        @Test
        @DisplayName("성공: 로그인 시도 횟수 조회")
        void getLoginAttempts_Success() {
            // given
            String email = "test@example.com";
            given(userRedisTemplete.getLoginAttempts(email)).willReturn(2);

            // when
            int attempts = authenticationService.getLoginAttempts(email);

            // then
            assertThat(attempts).isEqualTo(2);
            verify(userRedisTemplete).getLoginAttempts(email);
        }
    }
}