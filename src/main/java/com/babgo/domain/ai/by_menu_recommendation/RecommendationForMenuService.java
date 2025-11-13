package com.babgo.domain.ai.by_menu_recommendation;

import com.babgo.domain.ai.by_search_recommendation.AIRecommendationService;
import com.babgo.domain.menu.Menu;
import com.babgo.domain.store.Store;
import com.babgo.repository.menu.MenuRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationForMenuService {

    private final MenuRepositoryImpl menuRepository;
    private final AIRecommendationService aiRecommendationService;

    public List<RecommendedMenuDTO> recommendMenus(Long userId, UUID storeId, String userQuery, int topK) {

        Pageable pageable = PageRequest.of(0, topK); // 첫 페이지, 메뉴 개수 topK
        List<Menu> menus = menuRepository.findTopKAvailableMenusByStore(storeId, pageable);

        if (menus.isEmpty()) {
            return List.of(); // 가게에 메뉴가 없으면 빈 리스트 반환
        }

        Store store = menus.get(0).getStore();

        return menus.stream()
                .map(menu -> {
                    String reason = aiRecommendationService.generateMenuRecommendationReason(
                            userQuery,                // 🔥 사용자가 검색한 키워드 전달
                            menu.getName(),
                            store
                    );
                    return new RecommendedMenuDTO(
                            menu.getMenuId().toString(),
                            menu.getName(),
                            menu.getCategory(),
                            reason
                    );
                })
                .collect(Collectors.toList());
    }
}
