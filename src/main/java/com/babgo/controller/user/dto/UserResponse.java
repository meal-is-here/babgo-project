package com.babgo.controller.user.dto;

import com.babgo.domain.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class UserResponse {

    // 회원가입 응답 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignUpResponse {

        private String userId;
        private String email;
        private String name;
        private String nickname;
        private UserRole role;
        private LocalDateTime createdAt;
        private String message;  // 성공 메시지 (예: "회원가입이 완료되었습니다")

        public static SignUpResponse of(String userId, String email, String name,
                                        String nickname, UserRole role,
                                        LocalDateTime createdAt, String message) {
            return new SignUpResponse(userId, email, name, nickname, role, createdAt, message);
        }
    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private String accessToken;
        private String userId;
        private String email;
        private String name;
        private UserRole role;

        public static LoginResponse of(String accessToken, String userId,
                                       String email, String name, UserRole role) {
            return new LoginResponse(accessToken, userId, email, name, role);
        }
    }

    // TODO: 필요한 추가 응답 DTO를 작성하세요
    // - 토큰 갱신 응답 (Refresh Token 구현시)
}
