package com.babgo.application.menu;

import com.babgo.domain.menu.Menu;
import com.babgo.domain.menu.MenuService;
import com.babgo.domain.menu.MenuStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuFacade {

    private final MenuService menuService;

    // 메뉴 단건 조회
    public MenuInfo getMenu(UUID menuId) {
        Menu menu = menuService.getMenu(menuId);
        return MenuInfo.from(menu);
    }

    // 특정 가게 메뉴 전체 조회
    public List<MenuInfo> getMenusByStore(UUID storeId) {
        List<Menu> menus = menuService.getMenus(storeId);
        return menus.stream()
                .map(MenuInfo::from)
                .collect(Collectors.toList());
    }

    // 메뉴 생성
    @Transactional
    public MenuInfo createMenu(UUID storeId, String name, Long price, String description,
                               String category, String createdBy) {
        Menu menu = menuService.addMenu(storeId, name, price, description, category, createdBy);
        return MenuInfo.from(menu);
    }

    // 메뉴 상태 변경
    @Transactional
    public MenuInfo updateMenuStatus(UUID menuId, MenuStatus status, String updatedBy) {
        Menu menu = menuService.updateMenuStatus(menuId, status, updatedBy);
        return MenuInfo.from(menu);
    }

    // 메뉴 정보 변경
    @Transactional
    public MenuInfo updateMenuInfo(UUID menuId, String name, Long price, String description,
                                   String category, String updatedBy) {
        Menu menu = menuService.updateMenuInfo(menuId, name, price, description, category, updatedBy);
        return MenuInfo.from(menu);
    }

    // 메뉴 논리적 삭제
    @Transactional
    public MenuInfo deleteMenu(UUID menuId, String deletedBy) {
        Menu menu = menuService.deleteMenu(menuId, deletedBy);
        return MenuInfo.from(menu);
    }
}
