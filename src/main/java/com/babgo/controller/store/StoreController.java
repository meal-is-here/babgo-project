package com.babgo.controller.store;

import com.babgo.application.store.StoreFacade;
import com.babgo.domain.store.Store;
import com.babgo.global.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/stores")
public class StoreController {

    private final StoreFacade storeFacade;

    @PostMapping
    public ApiResponse<Void> createStore(@Validated({ValidateGroups.OnCreate.class, Default.class}) @RequestBody StoreRequest.Upsert storeRequest) {
        storeFacade.createStore(storeRequest.toCreateInfo());
        return ApiResponse.success("가게 등록을 성공했습니다.");
    }

    @PatchMapping("/{storeId}")
    public ApiResponse<Void> updateStore(@PathVariable UUID storeId, @Validated({ValidateGroups.OnUpdate.class, Default.class}) @RequestBody StoreRequest.Upsert storeRequest) {
        storeFacade.updateStore(storeId, storeRequest.toUpdateInfo());
        return ApiResponse.success("가게 수정을 성공했습니다.");
    }

    @DeleteMapping("/{storeId}")
    public ApiResponse<Void> deleteStore(@PathVariable UUID storeId) {
        storeFacade.deleteStore(storeId);
        return ApiResponse.success("가게 삭제를 성공했습니다.");
    }

    // 세준
    @GetMapping("/{storeId}")
    public ResponseEntity<Store> getStore(@PathVariable UUID id) {
        return storeFacade.getStoreById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 세준
    @GetMapping("/{storeId}/summary")
    public ResponseEntity<String> getSummary(@PathVariable UUID id) {
        String summary = storeFacade.getStoreSummary(id);
        return ResponseEntity.ok(summary);
    }
}

