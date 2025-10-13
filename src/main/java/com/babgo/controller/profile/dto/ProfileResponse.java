package com.babgo.controller.profile.dto;

import com.babgo.domain.user.User;
import lombok.Getter;

@Getter
public class ProfileResponse {

    private final String email;
    private final String name;
    private final String nickname;
    private final String phoneNumber;
    private final Boolean isProfilePublic;

    private ProfileResponse(String email, String name, String nickname, String phoneNumber, Boolean isProfilePublic) {
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.isProfilePublic = isProfilePublic;
    }

    public static ProfileResponse from(User user) {
        return new ProfileResponse(
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getPhoneNumber(),
                user.getIsProfilePublic()
        );
    }
}