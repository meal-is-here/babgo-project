package com.babgo.repository.menu;

import com.babgo.domain.menu.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import com.babgo.domain.menu.MenuRepository;

import java.util.List;
import java.util.UUID;

public interface MenuJpaRepository extends JpaRepository<Menu, UUID> {
    List<Menu> findByStore_StoreId(UUID storeId);
}
