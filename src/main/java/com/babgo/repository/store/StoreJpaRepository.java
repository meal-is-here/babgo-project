package com.babgo.repository.store;

import com.babgo.domain.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreJpaRepository extends JpaRepository<Store, UUID> {
    List<Store> findByStoreIdIn(Collection<UUID> storeIds);
}
