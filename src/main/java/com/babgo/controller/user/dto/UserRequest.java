package com.babgo.controller.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserRequest {

    // 고객 회원가입 요청 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerSignUpRequest {

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        private String password;

        @NotBlank(message = "이름은 필수입니다")
        private String name;

        private String nickname;

        @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다 (예: 010-1234-5678)")
        private String phoneNumber;

        // TODO: 고객 회원가입시 추가로 필요한 필드가 있다면 여기에 작성하세요
        // - 주소, 생년월일 등
    }

    // 가게(사장) 회원가입 요청 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerSignUpRequest {

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        private String password;

        @NotBlank(message = "이름은 필수입니다")
        private String name;

        private String nickname;

        @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다 (예: 010-1234-5678)")
        private String phoneNumber;

        // TODO: 사장 회원가입시 추가로 필요한 필드가 있다면 여기에 작성하세요
        // - 사업자 등록번호, 가게명 등
        private String businessNumber;  // 사업자 등록번호
        private String storeName;       // 가게명
    }

    // 로그인 요청 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        private String password;

        // 정적 팩토리 메서드: 회원가입 후 자동 로그인 시 사용
        public static LoginRequest of(String email, String password) {
            return new LoginRequest(email, password);
        }
    }
}
