package com.babgo.domain.store;

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
        when(categoryRepository.findByCategoryId(same(id))).thenReturn(Optional.of(category));

        // when
        Category result = categoryService.findByCategoryId(id);

        // then
        assertThat(result).isSameAs(category);
        verify(categoryRepository, times(1)).findByCategoryId(same(id));
    }

    @DisplayName("카테고리ID로 조회 시 존재하지 않으면 CustomException(NOT_FOUND)을 던진다.")
    @Test
    void findByCategoryId_throw_notFound() {
        // given
        UUID id = UUID.randomUUID();
        when(categoryRepository.findByCategoryId(eq(id))).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.findByCategoryId(id))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
                .extracting("customMessage")
                .isEqualTo("해당 카테고리를 찾을 수 없습니다.");

        verify(categoryRepository, times(1)).findByCategoryId(eq(id));
        verifyNoMoreInteractions(categoryRepository);
    }
}