package com.babgo.controller.user;

import com.babgo.domain.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserResponse {

    // 회원가입 응답 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignUpResponse {

        private UUID publicId;
        private String email;
        private String name;
        private String nickname;
        private UserRole role;
        private LocalDateTime createdAt;
        private String message;  // 성공 메시지 (예: "회원가입이 완료되었습니다")

        public static SignUpResponse of(UUID publicId, String email, String name,
                                        String nickname, UserRole role,
                                        LocalDateTime createdAt, String message) {
            return new SignUpResponse(publicId, email, name, nickname, role, createdAt, message);
        }
    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
        private UUID publicId;
        private String email;
        private String name;
        private UserRole role;

        public static LoginResponse of(String accessToken, String refreshToken, UUID publicId,
                                       String email, String name, UserRole role) {
            return new LoginResponse(accessToken, refreshToken, publicId, email, name, role);
        }
    }

    // 토큰 갱신 응답 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshTokenResponse {
        private String accessToken;
        private String refreshToken;

        public static RefreshTokenResponse of(String accessToken, String refreshToken) {
            return new RefreshTokenResponse(accessToken, refreshToken);
        }
    }
}
