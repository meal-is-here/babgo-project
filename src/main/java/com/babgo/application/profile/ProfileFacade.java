package com.babgo.application.profile;

import com.babgo.controller.profile.dto.ProfileResponse;
import com.babgo.domain.profile.ProfileService;
import com.babgo.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileFacade {

    private final ProfileService profileService;

    // get profile
    public ProfileResponse getMyProfile(String userId) {
        User user = profileService.getMyProfile(userId);
        return ProfileResponse.builder()
                .name(user.getName())
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .isProfilePublic(user.getIsProfilePublic())
                .build();
    }
}