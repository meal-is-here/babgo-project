package com.babgo.controller.profile.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileResponse {

    private final String name;
    private final String nickname;
    private final String phoneNumber;
    private final Boolean isProfilePublic;
}
