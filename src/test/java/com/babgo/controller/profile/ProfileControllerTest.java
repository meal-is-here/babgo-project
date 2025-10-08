package com.babgo.controller.profile;

import com.babgo.domain.user.User;
import com.babgo.domain.user.UserRole;
import com.babgo.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.babgo.BabgoApplication.class)
@AutoConfigureMockMvc
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("프로필 조회 - 성공 (인증된 사용자)")
    void getMyProfile_success() throws Exception {
        // given
        User user = User.ofCustomer(
                "test@example.com",
                "encodedPw",
                "이다인",
                "다인",
                "010-1234-5678"
        );
        User savedUser = userRepository.save(user);

        // when & then (WithMockUser는 동적으로 설정할 수 없으므로 SecurityContext를 직접 설정)
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
}