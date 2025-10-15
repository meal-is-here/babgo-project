package com.babgo.repository.menu;

import com.babgo.domain.menu.Menu;
import com.babgo.domain.menu.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuRepositoryImpl implements MenuRepository {

    private final MenuJpaRepository menuJpaRepository;

    @Override
    public Menu save(Menu menu) {
        return menuJpaRepository.save(menu);
    }

    @Override
    public Optional<Menu> findById(UUID menuId) {
        return menuJpaRepository.findById(menuId);
    }

    @Override
    public List<Menu> findByStore_StoreId(UUID storeId) {
        return menuJpaRepository.findByStore_StoreId(storeId);
    }
}
