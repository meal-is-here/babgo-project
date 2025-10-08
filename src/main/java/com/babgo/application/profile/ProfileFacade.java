package com.babgo.application.profile;

import com.babgo.controller.profile.dto.ProfileResponse;
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

    /**
     * 내 프로필 조회
     */
    public ProfileResponse getMyProfile(Long userId) {
        User user = userService.findByUserId(userId);
        return ProfileResponse.from(user);
    }

    /**
     * 프로필 수정
     */
    @Transactional
    public void updateProfile(Long userId, String nickname, String phoneNumber, Boolean isProfilePublic) {
        User user = userService.findByUserId(userId);
        profileService.updateProfile(user, nickname, phoneNumber, isProfilePublic);
    }
}