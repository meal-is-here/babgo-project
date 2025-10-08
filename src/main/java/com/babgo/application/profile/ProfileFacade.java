package com.babgo.application.profile;

import com.babgo.controller.profile.dto.ProfileResponse;
import com.babgo.controller.profile.dto.ProfileUpdateRequest;
import com.babgo.domain.profile.ProfileService;
import com.babgo.domain.user.User;
import com.babgo.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileFacade {

    private final UserService userService;
    private final ProfileService profileService;

    // read profile
    public ProfileResponse getMyProfile(Long userId) {
        return profileService.getMyProfile(userId);
    }

    // update profile
    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        return profileService.updateProfile(userId, request);
    }
}