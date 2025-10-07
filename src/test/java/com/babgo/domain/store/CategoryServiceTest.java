package com.babgo.domain.store;

import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    CategoryService categoryService;

    @DisplayName("카테고리ID로 카테고리를 찾아서 결과(Category)를 그대로 반환한다")
    @Test
    void findByCategoryId_success() {
        // given
        UUID id = UUID.randomUUID();
        Category category = mock(Category.class);
        when(categoryRepository.findByCategoryId(same(id))).thenReturn(category);

        // when
        Category result = categoryService.findByCategoryId(id);

        // then
        assertThat(result).isSameAs(category);
        verify(categoryRepository, times(1)).findByCategoryId(same(id));
    }

    @DisplayName("카테고리ID 조회 실패 시, CustomException(NOT_FOUND)을 그대로 전파한다")
    @Test
    void findByCategoryId_notFound() {
        // given
        UUID id = UUID.randomUUID();
        when(categoryRepository.findByCategoryId(same(id)))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> categoryService.findByCategoryId(id))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);

        verify(categoryRepository, times(1)).findByCategoryId(same(id));
        verifyNoMoreInteractions(categoryRepository);
    }
}