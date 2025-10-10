package com.babgo.controller.store;

import com.babgo.domain.ai.StoreSummaryService;
import com.babgo.application.store.StoreFacade;
import com.babgo.domain.store.Store;
import com.babgo.global.api.ApiResponse;
import com.babgo.repository.store.StoreRepositoryImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    // 세준
    @GetMapping("/{id}")
    public ResponseEntity<Store> getStore(@PathVariable UUID id) {
        return storeFacade.getStoreById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 세준
    @GetMapping("/{id}/summary")
    public ResponseEntity<String> getSummary(@PathVariable UUID id) {
        String summary = storeFacade.getStoreSummary(id);
        return ResponseEntity.ok(summary);
    }
}
