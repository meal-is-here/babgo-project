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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreFacadeTest {

    @Mock StoreService storeService;
    @Mock CategoryService categoryService;

    @InjectMocks StoreFacade storeFacade;

    @DisplayName("카테고리를 조회해 Store를 생성하고, storeService.create()를 호출한다.")
    @Test
    void createStore_success() {
        // given
        UUID categoryId = UUID.randomUUID();
        StoreInfo.Create input = StoreInfo.Create.of(
                "홍대 김치찌개", "서울시 마포구 양화로 123",
                37.5665, 126.9780, "123456","02-1234-5678", 15000,
                LocalTime.of(9, 0), LocalTime.of(21, 0), categoryId
        );
        Category category = mock(Category.class);
        when(categoryService.findByCategoryId(categoryId)).thenReturn(category);

        // when
        storeFacade.createStore(input);

        // then
        verify(categoryService, times(1)).findByCategoryId(categoryId);
        ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);
        verify(storeService, times(1)).create(storeCaptor.capture(), eq("userName"));

        Store created = storeCaptor.getValue();
        assertThat(created).isNotNull();
        assertThat(created.getCategory()).isSameAs(category);
        assertThat(created.getStoreName()).isEqualTo("홍대 김치찌개");
        assertThat(created.getAddressLine()).isEqualTo("서울시 마포구 양화로 123");
        assertThat(created.getLatitude()).isEqualTo(37.5665);
        assertThat(created.getLongitude()).isEqualTo(126.9780);
        assertThat(created.getRegionCode()).isEqualTo("123456");
        assertThat(created.getPhoneNumber()).isEqualTo("02-1234-5678");
        assertThat(created.getMinOrderAmount()).isEqualTo(15000);
        assertThat(created.getOpeningHours()).isEqualTo(LocalTime.of(9, 0));
        assertThat(created.getClosingHours()).isEqualTo(LocalTime.of(21, 0));

        verifyNoMoreInteractions(storeService, categoryService);
    }

    @DisplayName("updateStore: Store를 찾아서 변경할 값들만 Map에 담아 storeService.update()를 호출한다.")
    @Test
    void updateStore_success() {
        // given
        UUID storeId = UUID.randomUUID();
        Store store = mock(Store.class);
        when(storeService.findByStoreId(storeId)).thenReturn(store);

        UUID newCategoryId = UUID.randomUUID();
        StoreInfo.Update input = StoreInfo.Update.of(
                "새로운 가게명", "서울시 어딘가 123",
                37.5, 129.0,"123458",
                null, 20000,
                LocalTime.of(9, 0), LocalTime.of(22, 0),
                newCategoryId
        );

        // when
        storeFacade.updateStore(storeId, input);

        // then
        verify(storeService, times(1)).findByStoreId(storeId);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(storeService, times(1)).update(same(store), mapCaptor.capture(), eq("userName"));

        Map<String, Object> changes = mapCaptor.getValue();
        assertThat(changes).isNotNull();
        assertThat(changes).containsEntry("storeName", "새로운 가게명");
        assertThat(changes).containsEntry("addressLine", "서울시 어딘가 123");
        assertThat(changes).containsEntry("latitude", 37.5);
        assertThat(changes).containsEntry("longitude", 129.0);
        assertThat(changes).containsEntry("regionCode", "123458");
        assertThat(changes).doesNotContainKey("phoneNumber");
        assertThat(changes).containsEntry("minOrderAmount", 20000);
        assertThat(changes).containsEntry("openingHours", LocalTime.of(9, 0));
        assertThat(changes).containsEntry("closingHours", LocalTime.of(22, 0));
        assertThat(changes).containsEntry("categoryId", newCategoryId);

        verifyNoMoreInteractions(storeService);
        verifyNoInteractions(categoryService);
    }

    @DisplayName("deleteStore: Store를 찾아서 storeService.delete()를 호출한다.")
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
        verify(storeService, times(1)).delete(same(store), eq("userName"));
        verifyNoMoreInteractions(storeService);
        verifyNoInteractions(categoryService);
    }

    @DisplayName("deleteStore: storeService.delete에서 예외가 나면 그대로 전파한다.")
    @Test
    void deleteStore_throws_from_service() {
        // given
        UUID storeId = UUID.randomUUID();
        Store store = mock(Store.class);
        when(storeService.findByStoreId(storeId)).thenReturn(store);
        doThrow(new CustomException(ErrorCode.BAD_REQUEST, "이미 삭제된 가게입니다."))
                .when(storeService).delete(same(store), eq("userName"));

        // when & then
        CustomException ex = assertThrows(CustomException.class,
                () -> storeFacade.deleteStore(storeId));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
        assertThat(ex.getCustomMessage()).isEqualTo("이미 삭제된 가게입니다.");

        verify(storeService, times(1)).findByStoreId(storeId);
        verify(storeService, times(1)).delete(same(store), eq("userName"));
        verifyNoMoreInteractions(storeService);
        verifyNoInteractions(categoryService);
    }
}
