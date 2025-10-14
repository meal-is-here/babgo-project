package com.babgo.controller.store;

import com.babgo.application.store.StoreFacade;
import com.babgo.global.api.ApiResponse;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.babgo.controller.store.ValidateGroups.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/stores")
public class StoreController {

    private final StoreFacade storeFacade;

    @PostMapping
    public ApiResponse<Void> createStore(@Validated({OnCreate.class, Default.class}) @RequestBody StoreRequest.Upsert storeRequest) {
        storeFacade.createStore(storeRequest.toCreateInfo());
        return ApiResponse.success("가게 등록을 성공했습니다.");
    }

    @PatchMapping("/{storeId}")
    public ApiResponse<Void> updateStore(@PathVariable UUID storeId, @Validated({OnUpdate.class, Default.class}) @RequestBody StoreRequest.Upsert storeRequest) {
        storeFacade.updateStore(storeId, storeRequest.toUpdateInfo());
        return ApiResponse.success("가게 수정을 성공했습니다.");
    }

    @DeleteMapping("/{storeId}")
    public ApiResponse<Void> deleteStore(@PathVariable UUID storeId) {
        storeFacade.deleteStore(storeId);
        return ApiResponse.success("가게 삭제를 성공했습니다.");
    }
}
