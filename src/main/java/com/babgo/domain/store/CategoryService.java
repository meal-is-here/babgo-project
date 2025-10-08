package com.babgo.domain.store;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category findByCategoryId(UUID categoryId) {
        return categoryRepository.findByCategoryId(categoryId);
    }
}
