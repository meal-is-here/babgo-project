package com.babgo.application.profile;

import com.babgo.controller.profile.dto.ProfileUpdateRequest;
import lombok.Getter;

@Getter
public class ProfileInfo {
    private final String email;
    private final String password;
    private final String name;
    private final String nickname;
    private final String phoneNumber;
    private final Boolean isProfilePublic;

    private ProfileInfo(String email, String password, String name, String nickname, String phoneNumber, Boolean isProfilePublic) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.isProfilePublic = isProfilePublic;
    }

    public static ProfileInfo from(ProfileUpdateRequest request) {
        return new ProfileInfo(
                request.getEmail(),
                request.getPassword(),
                request.getName(),
                request.getNickname(),
                request.getPhoneNumber(),
                request.getIsProfilePublic()
        );
    }
}
