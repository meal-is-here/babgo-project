package com.babgo.domain.store;

import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category findByCategoryId(UUID categoryId) {
        return categoryRepository.findByCategoryId(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 카테고리를 찾을 수 없습니다."));
    }
}
