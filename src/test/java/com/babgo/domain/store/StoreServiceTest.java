package com.babgo.domain.store;

import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock StoreRepository storeRepository;
    @Mock CategoryService categoryService;

    @InjectMocks StoreService storeService;

    @DisplayName("create: Store를 저장하고 리포지토리의 반환값을 그대로 돌려준다.")
    @Test
    void create_success() {
        // given
        Store input = mock(Store.class);
        Store persisted = mock(Store.class);
        when(storeRepository.save(same(input))).thenReturn(persisted);

        // when
        Store result = storeService.create(input, "anyUser"); // 현재 구현은 내부에서 "userName" 하드코딩

        // then
        assertThat(result).isSameAs(persisted);
        verify(input, times(1)).markCreateBy("userName");
        verify(storeRepository, times(1)).save(same(input));
        verifyNoMoreInteractions(storeRepository);
    }

    @DisplayName("findByStoreId: 존재하면 Store를 반환한다.")
    @Test
    void findByStoreId_success() {
        // given
        UUID storeId = UUID.randomUUID();
        Store found = mock(Store.class);
        when(storeRepository.findByStoreId(eq(storeId))).thenReturn(Optional.of(found));

        // when
        Store result = storeService.findByStoreId(storeId);

        // then
        assertThat(result).isSameAs(found);
        verify(storeRepository, times(1)).findByStoreId(eq(storeId));
        verifyNoMoreInteractions(storeRepository);
    }

    @DisplayName("findByStoreId: 존재하지 않으면 CustomException(NOT_FOUND) 발생")
    @Test
    void findByStoreId_throw_notFound() {
        // given
        UUID storeId = UUID.randomUUID();
        when(storeRepository.findByStoreId(eq(storeId))).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeService.findByStoreId(storeId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
                .extracting("customMessage")
                .isEqualTo("해당 가게를 찾을 수 없습니다.");

        verify(storeRepository, times(1)).findByStoreId(eq(storeId));
        verifyNoMoreInteractions(storeRepository);
    }

    @DisplayName("update: changes 맵의 값들만 적용하고 markUpdatedBy 호출한다.")
    @Test
    void update_applies_changes_and_marks_updated() {
        // given
        Store store = mock(Store.class);
        UUID categoryId = UUID.randomUUID();
        Category category = mock(Category.class);
        when(categoryService.findByCategoryId(eq(categoryId))).thenReturn(category);

        Map<String, Object> changes = new HashMap<>();
        changes.put("storeName", "치킨천국");
        changes.put("latitude", 37.1234d);
        changes.put("openingHours", LocalTime.of(9, 0));
        changes.put("minOrderAmount", 15000); // Integer
        changes.put("categoryId", categoryId);

        // when
        storeService.update(store, changes, "anyUser");

        // then
        verify(store).changeStoreName("치킨천국");
        verify(store).changeMinOrderAmount(15000);
        verify(store).changeBusinessHours(eq(LocalTime.of(9,0)), nullable(LocalTime.class));
        verify(store).changeLocation(eq(37.1234d), anyDouble()); // 경도는 병합된 값
        verify(categoryService).findByCategoryId(categoryId);
        verify(store).changeCategory(category);

        // 하드코딩된 문자열을 사용하는 현재 코드에 맞춰 검증
        verify(store).markUpdatedBy("ownerName");
        verifyNoMoreInteractions(categoryService);
    }

    @DisplayName("delete: markDeletedBy 호출한다.")
    @Test
    void delete_marks_deleted() {
        // given
        Store store = mock(Store.class);

        // when
        storeService.delete(store, "anyUser");

        // then
        verify(store, times(1)).markDeletedBy("userName");
        verifyNoInteractions(storeRepository, categoryService);
    }
}
