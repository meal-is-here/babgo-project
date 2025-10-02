package com.babgo.controller.user;

import com.babgo.domain.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class UserResponse {

    // 회원가입 응답 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignUpResponse {

        private String userId;
        private String email;
        private String name;
        private String nickname;
        private UserRole role;
        private LocalDateTime createdAt;

        private String message;  // 성공 메시지 (예: "회원가입이 완료되었습니다")
    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginResponse {
        private String accessToken;
        private String userId;
        private String email;
        private String name;
        private UserRole role;
    }

    // 사용자 정보 조회 응답 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfoResponse {

        private String userId;
        private String email;
        private String name;
        private String nickname;
        private String phoneNumber;
        private UserRole role;
        private Boolean isProfilePublic;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    // TODO: 필요한 추가 응답 DTO를 작성하세요
    // - 토큰 갱신 응답 (Refresh Token 구현시)
    // - 사용자 목록 조회 응답
}
