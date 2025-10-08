package com.babgo.application.profile;

import com.babgo.controller.profile.dto.ProfileResponse;
import com.babgo.domain.profile.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileFacade {

    private final ProfileService profileService;

    // get profile
    public ProfileResponse getMyProfile(Long userId) {
        return profileService.getMyProfile(userId);
    }
}