package com.babgo.repository.store;

import com.babgo.domain.store.Category;
import com.babgo.domain.store.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public Optional<Category> findByCategoryId(UUID categoryId) {
        return categoryJpaRepository.findById(categoryId);
    }
}
