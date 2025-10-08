package com.babgo.search.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.babgo.application.search.SearchFasade;
import com.babgo.application.search.SearchInfo;
import com.babgo.controller.search.SearchController;
import com.babgo.controller.search.SearchSort;
import com.babgo.controller.search.SearchType;
import com.babgo.global.security.jwt.JwtTokenProvider;
import com.babgo.search.MockTest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(SearchController.class)
@ActiveProfiles("local")
@AutoConfigureMockMvc(addFilters = false) // 보안 필터 전부 비활성화
public class SearchControllerTest extends MockTest {


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchFasade searchFacade;

    @MockitoBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    AuditorAware<String> auditorAware;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void 검색_API_성공() throws Exception {

        // given 가짜 데이터 설정
        UUID categoryId = UUID.randomUUID();

        Mockito.when(searchFacade.getSearch(Mockito.any()))
            .thenReturn(List.of(
                    SearchInfo.CreateResult.of(UUID.randomUUID(), "교촌치킨", UUID.randomUUID(), "치킨", 4.8,
                        120, "OPEN"),
                    SearchInfo.CreateResult.of(UUID.randomUUID(), "BHC", UUID.randomUUID(), "치킨", 4.6,
                        98, "OPEN"),
                    SearchInfo.CreateResult.of(UUID.randomUUID(), "굽네치킨", UUID.randomUUID(), "치킨", 4.3,
                        70, "CLOSED")
                )
            );

        // when & then
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/search/stores")
                    .param("latitude", "37.5665")
                    .param("longitude", "126.9780")
                    .param("searchType", SearchType.KATEGORIE.name())
                    .param("keyword", categoryId.toString())
                    .param("sort", SearchSort.DISTANCE.name())
                    .param("page", "0")
                    .param("size", "10")
                    .accept(String.valueOf(MediaType.APPLICATION_JSON))
            )
            .andExpect(status().isOk())
            .andDo(print()) //
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("성공"))
            .andExpect(jsonPath("$.data").isArray());

    }

}
