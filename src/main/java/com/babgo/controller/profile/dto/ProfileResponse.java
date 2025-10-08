package com.babgo.controller.profile.dto;

import com.babgo.domain.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileResponse {

    private final String email;
    private final String name;
    private final String nickname;
    private final String phoneNumber;
    private final Boolean isProfilePublic;

    public static ProfileResponse from(User user) {
        return ProfileResponse.builder()
                              .email(user.getEmail())
                              .name(user.getName())
                              .nickname(user.getNickname())
                              .phoneNumber(user.getPhoneNumber())
                              .isProfilePublic(user.getIsProfilePublic())
                              .build();
    }
}