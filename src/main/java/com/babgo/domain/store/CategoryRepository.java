package com.babgo.domain.store;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Optional<Category> findByCategoryId(UUID categoryId);
}
