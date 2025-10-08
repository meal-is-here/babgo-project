package com.babgo.controller.profile;

import com.babgo.application.profile.ProfileFacade;
import com.babgo.controller.profile.dto.ProfileResponse;
import com.babgo.controller.profile.dto.ProfileUpdateRequest;
import com.babgo.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/profile")
public class ProfileController {

    private final ProfileFacade profileFacade;

    // read profile
    @GetMapping
    public ApiResponse<ProfileResponse> getMyProfile(@AuthenticationPrincipal Long userId) {
        ProfileResponse response = profileFacade.getMyProfile(userId);
        return ApiResponse.success("프로필 조회를 성공했습니다.", response);
    }

    // update profile
    @PatchMapping
    public ApiResponse<ProfileResponse> updateProfile(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        ProfileResponse response = profileFacade.updateProfile(userId, request);
        return ApiResponse.success("프로필 정보가 수정되었습니다.", response);
    }
}