package com.babgo.repository.store;

import com.babgo.domain.store.Category;
import com.babgo.domain.store.CategoryRepository;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public Category findByCategoryId(UUID categoryId) {
        return categoryJpaRepository.findById(categoryId).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND, "에러 메시지를 수정해주세요.")
        );
    }
}
