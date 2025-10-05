package com.babgo.controller.store;

import com.babgo.application.store.StoreFacade;
import com.babgo.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/stores")
public class StoreController {

    private final StoreFacade storeFacade;

    @PostMapping
    public ApiResponse<Void> createStore(@Valid @RequestBody StoreRequest.Create storeRequest) {
        storeFacade.createStore(storeRequest.toStoreInfo());
        return ApiResponse.success("가게 등록을 성공했습니다.");
    }

}
