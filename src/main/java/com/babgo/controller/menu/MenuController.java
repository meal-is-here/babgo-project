package com.babgo.controller.menu;

import com.babgo.application.menu.MenuService;
import com.babgo.domain.menu.Menu;
import com.babgo.controller.menu.dto.MenuRequest;
import com.babgo.controller.menu.dto.MenuResponse;
import com.babgo.domain.menu.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/stores/{storeId}/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

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

}
