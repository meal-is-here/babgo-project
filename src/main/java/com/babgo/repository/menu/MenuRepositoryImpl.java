package com.babgo.repository.menu;

import com.babgo.domain.menu.Menu;
import com.babgo.domain.menu.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    @Override
    public List<Menu> findAllById(List<UUID> targetMenuIds) {
        return menuJpaRepository.findAllById(targetMenuIds);
    }

    @Override
    public void saveAll(Collection<Menu> values) {
        menuJpaRepository.saveAll(values);
    }

    @Override
    public List<Menu> findTopKAvailableMenusByStore(UUID storeId, Pageable pageable) {
        return menuJpaRepository.findTopKAvailableMenusByStore(storeId,pageable);
    }

}
