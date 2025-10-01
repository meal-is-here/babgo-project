package com.babgo.application.menu;

import com.babgo.domain.menu.Menu;
import com.babgo.domain.menu.MenuRepository;
import com.babgo.domain.menu.MenuStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;
//    private final StoreRepository storeRepository;

    @Transactional
    public Menu addMenu(UUID storeId, String name, Long price, String description,
                        String category, String createBy) {

        // 스토어 조회 및, storeId 유효성 체크
//        Store store = storeRepository.findById(storeId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 스토어가 존재하지 않습니다."));

        Menu menu = new Menu(
                name,
                price,
                description,
                category,
                MenuStatus.AVAILABLE,
                createBy,
//                store
                storeId // store가 활성화 되면 비활성화 되어야 함
        );

        return menuRepository.save(menu);
    }

    public List<Menu> getMenus(UUID storeId) {
        return menuRepository.findByStoreId(storeId);
    }

    public Menu getMenu(UUID menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다."));
    }

    public Menu updateMenuStatus(UUID menuId, MenuStatus newStatus, String updatedBy) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다."));

        // 상태 변경
        menu.changeStatus(newStatus, updatedBy);

        return menuRepository.save(menu);
    }

    public Menu updateMenuInfo(UUID menuId, String name, Long price, String description,
                               String category, String updateBy) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다."));

        menu.updateMenuInfo(name, price, description, category, updateBy);
        return menuRepository.save(menu);
    }
}
