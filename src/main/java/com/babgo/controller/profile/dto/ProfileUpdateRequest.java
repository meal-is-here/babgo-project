package com.babgo.controller.profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ProfileUpdateRequest {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private final String email;

    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private final String password;

    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    @Size(min = 2, max = 20, message = "이름은 2자 이상이어야 합니다.")
    private final String name;

    @Size(min = 2, max = 20, message = "닉네임은 2자 이상, 20자 이하여야 합니다.")
    private final String nickname;

    @Pattern(regexp = "^010\\d{8}$", message = "전화번호 형식이 올바르지 않습니다.")
    private final String phoneNumber;

    private final Boolean isProfilePublic;

    public ProfileUpdateRequest(String email, String password, String name, String nickname, String phoneNumber, Boolean isProfilePublic) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.isProfilePublic = isProfilePublic;
    }
}