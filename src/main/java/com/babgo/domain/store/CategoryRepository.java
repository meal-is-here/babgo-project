package com.babgo.domain.store;

import java.util.UUID;

public interface CategoryRepository {
    Category findByCategoryId(UUID categoryId);
}
