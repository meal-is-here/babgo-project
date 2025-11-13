package com.babgo.repository.menu;

import com.babgo.domain.menu.Menu;
import feign.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.babgo.domain.menu.MenuRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MenuJpaRepository extends JpaRepository<Menu, UUID> {
    List<Menu> findByStore_StoreId(UUID storeId);

    @Query("SELECT m FROM Menu m WHERE m.store.storeId = :storeId AND m.menuStatus = 'AVAILABLE'")
    List<Menu> findTopKAvailableMenusByStore(@Param("storeId") UUID storeId, Pageable pageable);
}
