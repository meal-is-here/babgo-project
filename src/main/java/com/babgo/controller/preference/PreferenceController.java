package com.babgo.controller.preference;

import com.babgo.controller.preference.dto.PreferenceResponse;
import com.babgo.domain.preference.PreferenceService;
import com.babgo.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/preferences")
public class PreferenceController {

    private final PreferenceService preferenceService;

    @GetMapping
    public ResponseEntity<ApiResponse<PreferenceResponse>> getUserPreferences(
            // TODO: 인증 추가 예정
            // @AuthenticationPrincipal Long userId
    ) {
        Long userId = 1L;
        PreferenceResponse response = preferenceService.getUserPreferences(userId);
        return ResponseEntity.ok(ApiResponse.success("좋아요/즐겨찾기 목록 조회 성공", response));
    }
}
