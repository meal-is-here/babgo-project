package com.babgo.controller.store;

import com.babgo.application.store.StoreFacade;
import com.babgo.application.store.StoreInfo;
import com.babgo.domain.user.User;
import com.babgo.domain.user.UserRole;
import com.babgo.global.api.ApiResponse;
import com.babgo.global.security.annotation.CurrentUser;
import com.babgo.global.security.annotation.RequireRole;
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

    @RequireRole(UserRole.OWNER)
    @PostMapping
    public ApiResponse<Void> createStore(
            @CurrentUser User user,
            @Validated({ValidateGroups.OnCreate.class, Default.class})
            @RequestBody StoreRequest.Upsert storeRequest
    ) {
        storeFacade.createStore(user,storeRequest.toCreateInfo());
        return ApiResponse.success("가게 등록을 성공했습니다.");
    }

    @RequireRole({UserRole.MANAGER,UserRole.OWNER,UserRole.MASTER})
    @PatchMapping("/{storeId}")
    public ApiResponse<Void> updateStore(
            @CurrentUser User user,
            @PathVariable("storeId") UUID storeId,
            @Validated({ValidateGroups.OnUpdate.class, Default.class})
            @RequestBody StoreRequest.Upsert storeRequest
    ) {
        storeFacade.updateStore(user,storeId, storeRequest.toUpdateInfo());
        return ApiResponse.success("가게 수정을 성공했습니다.");
    }

    @RequireRole({UserRole.MANAGER,UserRole.OWNER,UserRole.MASTER})
    @DeleteMapping("/{storeId}")
    public ApiResponse<Void> deleteStore(
            @CurrentUser User user,
            @PathVariable("storeId") UUID storeId
    ) {
        storeFacade.deleteStore(user,storeId);
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

    @RequireRole({UserRole.MANAGER,UserRole.OWNER,UserRole.MASTER})
    @PatchMapping("/orders/{orderId}/prepared")
    public ApiResponse<StoreResponse.OrderStatusResult> prepareOrder(
            @CurrentUser User user,
            @PathVariable("orderId") UUID orderId
    ) {
        StoreInfo.OrderStatusResult output = storeFacade.preparedOrder(user,orderId);
        StoreResponse.OrderStatusResult response = StoreResponse.OrderStatusResult.from(output);
        return ApiResponse.success("조리 완료 되었습니다.", response);
    }

    @RequireRole({UserRole.MANAGER,UserRole.OWNER,UserRole.MASTER})
    @PatchMapping("/orders/{orderId}/picked-up")
    public ApiResponse<StoreResponse.OrderStatusResult> pickUpOrder(
            @CurrentUser User user,
            @PathVariable("orderId") UUID orderId
    ) {
        StoreInfo.OrderStatusResult output = storeFacade.pickedUpOrder(user,orderId);
        StoreResponse.OrderStatusResult response = StoreResponse.OrderStatusResult.from(output);
        return ApiResponse.success("음식이 픽업되었습니다.", response);
    }

    @RequireRole({UserRole.MANAGER,UserRole.OWNER,UserRole.MASTER})
    @PatchMapping("/orders/{orderId}/delivered")
    public ApiResponse<StoreResponse.OrderStatusResult> deliverOrder(
            @CurrentUser User user,
            @PathVariable("orderId") UUID orderId
    ) {
        StoreInfo.OrderStatusResult output = storeFacade.deliveredOrder(user,orderId);
        StoreResponse.OrderStatusResult response = StoreResponse.OrderStatusResult.from(output);
        return ApiResponse.success("배달이 완료되었습니다.", response);
    }
}