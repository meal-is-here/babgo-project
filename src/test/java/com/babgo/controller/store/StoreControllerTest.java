package com.babgo.controller.store;

import com.babgo.application.store.StoreFacade;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.LocalTime;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StoreController.class)
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StoreFacade storeFacade;

    @MockitoBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    AuditorAware<String> auditorAware;

    @DisplayName("POST /v1/stores - 가게등록 성공")
    @Test
    void createStore() throws Exception {
        StoreRequest.Create request = StoreRequest.Create.of(
                "버거프렌즈",
                "서울시 강남구 테헤란로 123",
                37.4979,
                127.0276,
                "010-1234-5678",
                12000,
                LocalTime.of(9, 0),
                LocalTime.of(21, 0),
                UUID.randomUUID()
        );

        mockMvc.perform(
                        post("/v1/stores")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("가게 등록을 성공했습니다."));

        verify(storeFacade, times(1)).createStore(any());
    }

}
