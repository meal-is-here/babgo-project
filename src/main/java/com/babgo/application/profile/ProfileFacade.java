package com.babgo.application.profile;

import com.babgo.controller.profile.dto.ProfileResponse;
import com.babgo.controller.profile.dto.ProfileUpdateRequest;
import com.babgo.domain.profile.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProfileFacade {

    private final ProfileService profileService;

    // read profile
    public ProfileResponse getMyProfile(Long userId) {
        return profileService.getMyProfile(userId);
    }

    // update profile
    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        ProfileInfo info = ProfileInfo.from(request);
        return profileService.updateProfile(userId, info);
    }

    // delete profile
    public void deleteProfile(Long userId) {
        profileService.deleteProfile(userId);
    }
}