package com.babgo.repository.search;

import com.babgo.domain.search.Search;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SearchJpaRepository extends JpaRepository<Search, UUID> {


    Search findByStoreId(UUID storeId);
}
