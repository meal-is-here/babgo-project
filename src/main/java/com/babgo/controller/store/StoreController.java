package com.babgo.controller.store;

import com.babgo.application.store.StoreInfo;
import com.babgo.application.store.StoreFacade;
import com.babgo.global.api.ApiResponse;
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

    // 가게 단건 조회
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreInfo.Detail> getStore(@PathVariable UUID storeId) {
        try {
            StoreInfo.Detail detail = storeFacade.getStoreById(storeId);
            return ResponseEntity.ok(detail);  // JSON 그대로 반환
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    // 가게 리뷰 요약 조회 (JSON 반환)
    @GetMapping("/{storeId}/summary")
    public ResponseEntity<StoreInfo.Summary> getSummary(@PathVariable UUID storeId) {
        try {
            StoreInfo.Summary summary = storeFacade.getStoreSummary(storeId);
            return ResponseEntity.ok(summary); // {"summary":"..."}
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(StoreInfo.Summary.of("STORE_NOT_FOUND"));
        }
    }
}
