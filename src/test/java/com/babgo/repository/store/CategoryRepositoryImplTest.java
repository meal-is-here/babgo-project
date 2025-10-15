package com.babgo.repository.store;

import com.babgo.domain.store.Category;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryRepositoryImplTest {

    @Mock
    CategoryJpaRepository categoryJpaRepository;

    @InjectMocks
    CategoryRepositoryImpl categoryRepositoryImpl;

    @DisplayName("카테고리ID로 조회해서 존재하면 해당 엔티티를 그대로 반환한다")
    @Test
    void findByCategoryId_found() {
        // given
        UUID id = UUID.randomUUID();
        Category category = mock(Category.class);
        when(categoryJpaRepository.findById(same(id))).thenReturn(Optional.of(category));

        // when
        Category result = categoryRepositoryImpl.findByCategoryId(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // then
        assertThat(result).isSameAs(category);
        verify(categoryJpaRepository, times(1)).findById(same(id));
    }
}