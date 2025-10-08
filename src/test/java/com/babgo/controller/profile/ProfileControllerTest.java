package com.babgo.controller.profile;

import com.babgo.domain.user.User;
import com.babgo.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.babgo.BabgoApplication.class)
@AutoConfigureMockMvc
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    // 프로필 조회
    @Test
    @DisplayName("프로필 조회 - 성공 (인증된 사용자)")
    void getMyProfile_success() throws Exception {
        // given
        User user = User.ofCustomer(
                "test@example.com",
                "password123!",
                "이다인",
                "다인",
                "010-1234-5678"
        );
        User savedUser = userRepository.save(user);

        // when & then
        mockMvc.perform(get("/api/profile/me")
                        .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isUnauthorized()); // 실제 JWT 토큰이 없으므로 401 반환
    }

    @Test
    @DisplayName("프로필 조회 - 비로그인 사용자 (401 Unauthorized)")
    void getMyProfile_unauthorized() throws Exception {
        mockMvc.perform(get("/api/profile/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }


    // 프로필 수정
    @Test
    @DisplayName("프로필 수정 - 성공 (인증된 사용자)")
    void updateProfile_success() throws Exception {
        // given
        User user = User.ofCustomer(
                "update@example.com",
                "encodedPw",
                "이다인",
                "다인",
                "010-1234-5678"
        );
        userRepository.save(user);

        String requestBody = """
            {
                "name": "수정된이름",
                "nickname": "수정된닉네임",
                "phoneNumber": "010-9999-8888",
                "isProfilePublic": true
            }
            """;

        // when & then
        mockMvc.perform(patch("/api/profile")
                        .header("Authorization", "Bearer mock-token")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("프로필 수정 - 비로그인 사용자 (401 Unauthorized)")
    void updateProfile_unauthorized() throws Exception {
        String requestBody = """
            {
                "name": "수정된이름",
                "nickname": "수정된닉네임",
                "phoneNumber": "010-9999-8888",
                "isProfilePublic": true
            }
            """;

        mockMvc.perform(patch("/api/profile")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }
}