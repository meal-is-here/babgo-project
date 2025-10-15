package com.babgo.controller.menu;

import com.babgo.application.menu.MenuFacade;
import com.babgo.application.menu.MenuInfo;
import com.babgo.controller.menu.dto.MenuRequest;
import com.babgo.controller.menu.dto.MenuResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/stores/{storeId}/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuFacade menuFacade;

    // 메뉴 생성
    @PostMapping
    public MenuResponse addMenu(@PathVariable UUID storeId, @RequestBody MenuRequest request) {
        // Facade에 Command DTO 혹은 Entity 변환 없이 바로 MenuInfo 반환
        MenuInfo menuInfo = menuFacade.createMenu(
                storeId,
                request.getName(),
                request.getPrice(),
                request.getDescription(),
                request.getCategory(),
                request.getStock(),
                request.getCreatedBy()
        );

        return toMenuResponse(menuInfo);
    }

    // 메뉴 전체 조회
    @GetMapping
    public List<MenuResponse> getMenus(@PathVariable UUID storeId) {
        List<MenuInfo> menus = menuFacade.getMenusByStore(storeId);
        return menus.stream()
                .map(this::toMenuResponse)
                .collect(Collectors.toList());
    }

    // 메뉴 단건 조회
    @GetMapping("/{menuId}")
    public MenuResponse getMenu(@PathVariable UUID menuId) {
        MenuInfo menuInfo = menuFacade.getMenu(menuId);
        return toMenuResponse(menuInfo);
    }

    // 메뉴 상태 변경
    @PatchMapping("/{menuId}/status")
    public MenuResponse updateMenuStatus(@PathVariable UUID menuId,
                                         @RequestBody MenuRequest request) {

        MenuInfo menuInfo = menuFacade.updateMenuStatus(menuId, request.getStatus(), request.getUpdatedBy());
        return toMenuResponse(menuInfo);
    }

    // 메뉴 정보 변경
    @PutMapping("/{menuId}")
    public MenuResponse updateMenuInfo(@PathVariable UUID menuId,
                                       @RequestBody MenuRequest request) {

        MenuInfo menuInfo = menuFacade.updateMenuInfo(
                menuId,
                request.getName(),
                request.getPrice(),
                request.getDescription(),
                request.getCategory(),
                request.getUpdatedBy()
        );

        return toMenuResponse(menuInfo);
    }

    // 메뉴 논리적 삭제
    @DeleteMapping("/{menuId}")
    public MenuResponse deleteMenu(@PathVariable UUID menuId,
                                   @RequestBody MenuRequest request) {

        MenuInfo menuInfo = menuFacade.deleteMenu(menuId, request.getUpdatedBy());
        return toMenuResponse(menuInfo);
    }

    // 재고 차감
    @PatchMapping("/{menuId}/stock/decrease")
    public MenuResponse decreaseStock(@PathVariable UUID storeId,
                                      @PathVariable UUID menuId,
                                      @RequestBody MenuRequest request) {
        MenuInfo menuInfo = menuFacade.decreaseStock(menuId, request.getQuantity());
        return toMenuResponse(menuInfo);
    }

    // 재고 증가
    @PatchMapping("/{menuId}/stock/increase")
    public MenuResponse increaseStock(@PathVariable UUID storeId,
                                      @PathVariable UUID menuId,
                                      @RequestBody MenuRequest request) {
        MenuInfo menuInfo = menuFacade.increaseStock(menuId, request.getQuantity());
        return toMenuResponse(menuInfo);
    }

    // MenuInfo → MenuResponse 변환 헬퍼
    private MenuResponse toMenuResponse(MenuInfo info) {
        return new MenuResponse(
                info.getMenuId(),
                info.getName(),
                info.getPrice(),
                info.getDescription(),
                info.getCategory(),
                info.getStatus(),
                info.getStock()
        );
    }
}


//package com.babgo.controller.menu;
//
//import com.babgo.domain.menu.MenuService;
//import com.babgo.domain.menu.Menu;
//import com.babgo.controller.menu.dto.MenuRequest;
//import com.babgo.controller.menu.dto.MenuResponse;
//import com.babgo.domain.menu.MenuStatus;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/v1/stores/{storeId}/menus")
//@RequiredArgsConstructor
//public class MenuController {
//
//    private final MenuService menuService;
//
//    // 메뉴 생성
//    @PostMapping
//    public MenuResponse addMenu(@PathVariable UUID storeId, @RequestBody MenuRequest request) {
//        Menu menu = menuService.addMenu(
//                storeId,
//                request.getName(),
//                request.getPrice(),
//                request.getDescription(),
//                request.getCategory(),
//                request.getCreatedBy()
//        );
//
//        return new MenuResponse(
//                menu.getMenuId(),
//                menu.getName(),
//                menu.getPrice(),
//                menu.getDescription(),
//                menu.getCategory(),
//                menu.getMenuStatus()
//        );
//    }
//
//    // 메뉴 전체 조회
//    @GetMapping
//    public List<MenuResponse> getMenus(@PathVariable UUID storeId) {
//        return menuService.getMenus(storeId)
//                .stream()
//                .map(m -> new MenuResponse(
//                        m.getMenuId(),
//                        m.getName(),
//                        m.getPrice(),
//                        m.getDescription(),
//                        m.getCategory(),
//                        m.getMenuStatus()
//                ))
//                .toList();
//    }
//
//    // 메뉴 단건 조회
//    @GetMapping("{menuId}")
//    public MenuResponse getMenu(@PathVariable UUID menuId) {
//        Menu menu =  menuService.getMenu(menuId);
//        return new MenuResponse(
//                menu.getMenuId(),
//                menu.getName(),
//                menu.getPrice(),
//                menu.getDescription(),
//                menu.getCategory(),
//                menu.getMenuStatus()
//        );
//    }
//
//    // 메뉴 상태 변경
//    @PatchMapping("/{menuId}/status")
//    public MenuResponse updateMenuStatus(// @PathVariable UUID storeId,
//                                         @PathVariable UUID menuId,
//                                         @RequestBody MenuRequest request) {
//
//        MenuStatus newStatus = request.getStatus();
//        String updatedBy = request.getUpdatedBy();
//
//        Menu menu = menuService.updateMenuStatus(menuId, newStatus, updatedBy);
//
//        return new MenuResponse(
//                menu.getMenuId(),
//                menu.getName(),
//                menu.getPrice(),
//                menu.getDescription(),
//                menu.getCategory(),
//                menu.getMenuStatus()
//        );
//    }
//
//    // 메뉴 정보 변경
//    @PutMapping("/{menuId}")
//    public MenuResponse updateMenuInfo(@PathVariable UUID storeId,
//                                       @PathVariable UUID menuId,
//                                       @RequestBody MenuRequest request) {
//        Menu menu = menuService.updateMenuInfo(
//                menuId,
//                request.getName(),
//                request.getPrice(),
//                request.getDescription(),
//                request.getCategory(),
//                request.getUpdatedBy()
//        );
//
//        return new MenuResponse(
//                menu.getMenuId(),
//                menu.getName(),
//                menu.getPrice(),
//                menu.getDescription(),
//                menu.getCategory(),
//                menu.getMenuStatus()
//        );
//    }
//
//    // 메뉴 논리적 삭제
//    @DeleteMapping("/{menuId}")
//    public MenuResponse deleteMenu(// @PathVariable UUID storeId,
//                                   @PathVariable UUID menuId,
//                                   @RequestBody MenuRequest request) {
//        Menu menu = menuService.deleteMenu(menuId, request.getUpdatedBy());
//
//        return new MenuResponse(
//                menu.getMenuId(),
//                menu.getName(),
//                menu.getPrice(),
//                menu.getDescription(),
//                menu.getCategory(),
//                menu.getMenuStatus()
//        );
//    }
//}
