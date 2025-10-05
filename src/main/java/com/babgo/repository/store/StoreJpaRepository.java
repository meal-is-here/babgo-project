package com.babgo.repository.store;

import com.babgo.domain.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreJpaRepository extends JpaRepository<Store, UUID> {
}
