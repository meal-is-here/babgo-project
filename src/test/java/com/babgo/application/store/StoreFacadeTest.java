package com.babgo.application.store;

import com.babgo.domain.store.Category;
import com.babgo.domain.store.CategoryService;
import com.babgo.domain.store.Store;
import com.babgo.domain.store.StoreService;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreFacadeTest {

    @Mock
    StoreService storeService;
    @Mock
    CategoryService categoryService;

    @InjectMocks
    StoreFacade storeFacade;

    @DisplayName("카테고리를 조회해서 가게를 생성,저장한다.")
    @Test
    void createStore_success() {
        // given
        UUID categoryId = UUID.randomUUID();
        StoreInfo.Create input = StoreInfo.Create.of(
                "홍대 김치찌개", "서울시 마포구 양화로 123",
                37.5665, 126.9780, "02-1234-5678", 15000,
                LocalTime.of(9, 0), LocalTime.of(21, 0), categoryId
        );
        Category category = mock(Category.class);
        when(categoryService.findByCategoryId(categoryId)).thenReturn(category);
        // when
        storeFacade.createStore(input);
        // then
        verify(categoryService, times(1)).findByCategoryId(categoryId);
        ArgumentCaptor<Store> captor = ArgumentCaptor.forClass(Store.class);
        verify(storeService, times(1)).create(captor.capture());
        Store store = captor.getValue();
        assertThat(store.getCategory()).isSameAs(category);
        assertThat(store.getCreatedBy()).isEqualTo("ownerName");
    }

    @DisplayName("가게 수정시, 수정할 필드 값들이 오면 해당가게의 필드값들을 수정한다.")
    @Test
    void updateStore_success() {
        // given
        UUID storeId = UUID.randomUUID();
        UUID newCategoryId = UUID.randomUUID();

        Store store = mock(Store.class);
        when(storeService.findByStoreId(storeId)).thenReturn(store);

        Category newCategory = mock(Category.class);
        when(categoryService.findByCategoryId(newCategoryId)).thenReturn(newCategory);

        StoreInfo.Update input = StoreInfo.Update.of(
                "새로운 가게명", "서울시 어딘가 123",
                37.5, 129.0,
                null, 20000,
                LocalTime.of(9,0), LocalTime.of(22,0),
                newCategoryId
        );

        // when
        storeFacade.updateStore(storeId, input);

        // then
        verify(storeService).findByStoreId(storeId);
        verify(store).changeStoreName("새로운 가게명");
        verify(store).changeAddressLine("서울시 어딘가 123");
        verify(store).changeLocation(37.5, 129.0);
        verify(store, never()).changePhoneNumber(any());
        verify(store).changeMinOrderAmount(20000);
        verify(store).changeBusinessHours(LocalTime.of(9,0), LocalTime.of(22,0));
        verify(categoryService).findByCategoryId(newCategoryId);
        verify(store).changeCategory(newCategory);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(store).markUpdatedBy(captor.capture());
        assertThat(captor.getValue()).isEqualTo("ownerName");
    }

    @DisplayName("해당 가게를 삭제할 수 있다.")
    @Test
    void deleteStore_success() {
        // given
        UUID storeId = UUID.randomUUID();
        Store store = mock(Store.class);
        when(storeService.findByStoreId(storeId)).thenReturn(store);

        // when
        storeFacade.deleteStore(storeId);

        // then
        verify(storeService, times(1)).findByStoreId(storeId);
        verify(store, times(1)).markDeletedBy("ownerName");
        verifyNoMoreInteractions(storeService, store);
    }

    @DisplayName("이미 삭제된 가게는 삭제 시 예외가 발생한다")
    @Test
    void deleteStore_alreadyDeleted_throws() {
        // given
        UUID storeId = UUID.randomUUID();
        Store store = mock(Store.class);
        when(storeService.findByStoreId(storeId)).thenReturn(store);
        doThrow(new CustomException(ErrorCode.BAD_REQUEST, "이미 삭제된 가게입니다."))
                .when(store).markDeletedBy("ownerName");

        // when & then
        CustomException ex = assertThrows(CustomException.class,
                () -> storeFacade.deleteStore(storeId));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
        assertThat(ex.getCustomMessage()).isEqualTo("이미 삭제된 가게입니다.");

        verify(storeService, times(1)).findByStoreId(storeId);
        verify(store, times(1)).markDeletedBy("ownerName");
        verifyNoMoreInteractions(storeService, store);
    }

}