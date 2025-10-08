package com.babgo.controller.profile;

import com.babgo.application.profile.ProfileFacade;
import com.babgo.controller.profile.dto.ProfileResponse;
import com.babgo.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileFacade profileFacade;

    // get profile
    @GetMapping("/me")
    public ApiResponse<ProfileResponse> getMyProfile(@AuthenticationPrincipal String userId) {
        return ApiResponse.success(profileFacade.getMyProfile(userId));
    }
}