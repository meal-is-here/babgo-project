package com.babgo.repository.store;

import com.babgo.domain.store.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryJpaRepository extends JpaRepository<Category, UUID> {

}
