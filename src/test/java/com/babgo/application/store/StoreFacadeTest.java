package com.babgo.application.store;

import com.babgo.domain.store.Category;
import com.babgo.domain.store.CategoryService;
import com.babgo.domain.store.Store;
import com.babgo.domain.store.StoreService;
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
        UUID categoryId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        StoreInfo.Create input = StoreInfo.Create.of(
                "홍대 김치찌개",
                "서울시 마포구 양화로 123",
                37.5665,
                126.9780,
                "02-1234-5678",
                15000,
                LocalTime.of(9, 0),
                LocalTime.of(21, 0),
                categoryId
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

        assertThat(store.getStoreName()).isEqualTo("홍대 김치찌개");
        assertThat(store.getAddressLine()).isEqualTo("서울시 마포구 양화로 123");
        assertThat(store.getPhoneNumber()).isEqualTo("02-1234-5678");
        assertThat(store.getLatitude()).isEqualTo(37.5665);
        assertThat(store.getLongitude()).isEqualTo(126.9780);
        assertThat(store.getPhoneNumber()).isEqualTo("02-1234-5678");
        assertThat(store.getMinOrderAmount()).isEqualTo(15000);
        assertThat(store.getOpeningHours()).isEqualTo(LocalTime.of(9, 0));
        assertThat(store.getClosingHours()).isEqualTo(LocalTime.of(21, 0));
        assertThat(store.getCategory()).isSameAs(category);
        }
}