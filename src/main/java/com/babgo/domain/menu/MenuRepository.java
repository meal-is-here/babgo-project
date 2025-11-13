package com.babgo.domain.menu;

import feign.Param;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuRepository {
    Menu save(Menu menu);
    Optional<Menu> findById(UUID menuId);
    List<Menu> findByStore_StoreId(UUID storeId);
    List<Menu> findAllById(List<UUID> targetMenuIds);
    void saveAll(Collection<Menu> values);

    List<Menu> findTopKAvailableMenusByStore(@Param("storeId") UUID storeId, Pageable pageable);

}
