package com.babgo.controller.profile;

import com.babgo.controller.profile.dto.ProfileResponse;
import com.babgo.domain.profile.ProfileService;
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

    private final ProfileService profileService;

    @GetMapping("/me")
    public ApiResponse<ProfileResponse> getMyProfile(@AuthenticationPrincipal Long userId) {
        ProfileResponse response = profileService.getMyProfile(userId);
        return ApiResponse.success("프로필 조회를 성공했습니다.", response);
    }
}
