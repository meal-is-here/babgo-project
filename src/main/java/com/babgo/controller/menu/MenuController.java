package com.babgo.controller.menu;

import com.babgo.application.menu.MenuService;
import com.babgo.domain.menu.Menu;
import com.babgo.controller.menu.dto.MenuRequest;
import com.babgo.controller.menu.dto.MenuResponse;
import com.babgo.domain.menu.MenuRepository;
import com.babgo.domain.menu.MenuStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/stores/{storeId}/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // 메뉴 생성
    @PostMapping
    public MenuResponse addMenu(@PathVariable UUID storeId, @RequestBody MenuRequest request) {
        Menu menu = menuService.addMenu(
                storeId,
                request.getName(),
                request.getPrice(),
                request.getDescription(),
                request.getCategory(),
                request.getCreatedBy()
        );

        return new MenuResponse(
                menu.getMenuId(),
                menu.getName(),
                menu.getPrice(),
                menu.getDescription(),
                menu.getCategory(),
                menu.getMenuStatus()
        );
    }

    // 메뉴 전체 조회
    @GetMapping
    public List<MenuResponse> getMenus(@PathVariable UUID storeId) {
        return menuService.getMenus(storeId)
                .stream()
                .map(m -> new MenuResponse(
                        m.getMenuId(),
                        m.getName(),
                        m.getPrice(),
                        m.getDescription(),
                        m.getCategory(),
                        m.getMenuStatus()
                ))
                .toList();
    }

    // 메뉴 단건 조회
    @GetMapping("{menuId}")
    public MenuResponse getMenu(@PathVariable UUID menuId) {
        Menu menu =  menuService.getMenu(menuId);
        return new MenuResponse(
                menu.getMenuId(),
                menu.getName(),
                menu.getPrice(),
                menu.getDescription(),
                menu.getCategory(),
                menu.getMenuStatus()
        );
    }

    // 메뉴 상태 변경
    @PatchMapping("/{menuId}/status")
    public MenuResponse updateMenuStatus(// @PathVariable UUID storeId,
                                         @PathVariable UUID menuId,
                                         @RequestBody MenuRequest request) {

        MenuStatus newStatus = request.getStatus();
        String updatedBy = request.getUpdatedBy();

        Menu menu = menuService.updateMenuStatus(menuId, newStatus, updatedBy);

        return new MenuResponse(
                menu.getMenuId(),
                menu.getName(),
                menu.getPrice(),
                menu.getDescription(),
                menu.getCategory(),
                menu.getMenuStatus()
        );
    }
}
