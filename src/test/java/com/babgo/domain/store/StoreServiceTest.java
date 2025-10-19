package com.babgo.domain.store;

import com.babgo.domain.order.Order;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock StoreRepository storeRepository;
    @Mock CategoryService categoryService;
    @Mock Order order;

    @InjectMocks StoreService storeService;

    @DisplayName("create: Store를 저장하고 리포지토리의 반환값을 그대로 돌려준다.")
    @Test
    void create_success() {
        // given
        Store input = mock(Store.class);
        Store persisted = mock(Store.class);
        when(storeRepository.save(same(input))).thenReturn(persisted);
        String userName = "anyUser";

        // when
        Store result = storeService.create(input, userName);

        // then
        assertThat(result).isSameAs(persisted);
        verify(input).markCreateBy(userName);
        verify(storeRepository).save(same(input));
        verifyNoMoreInteractions(storeRepository);
    }

    @DisplayName("findByStoreId: 존재하면 Store를 반환한다.")
    @Test
    void findByStoreId_success() {
        UUID storeId = UUID.randomUUID();
        Store found = mock(Store.class);
        when(storeRepository.findByStoreId(eq(storeId))).thenReturn(Optional.of(found));

        Store result = storeService.findByStoreId(storeId);

        assertThat(result).isSameAs(found);
        verify(storeRepository).findByStoreId(eq(storeId));
        verifyNoMoreInteractions(storeRepository);
    }

    @DisplayName("findByStoreId: 존재하지 않으면 CustomException(NOT_FOUND) 발생")
    @Test
    void findByStoreId_throw_notFound() {
        UUID storeId = UUID.randomUUID();
        when(storeRepository.findByStoreId(eq(storeId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeService.findByStoreId(storeId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
                .extracting("customMessage").isEqualTo("해당 가게를 찾을 수 없습니다.");

        verify(storeRepository).findByStoreId(eq(storeId));
        verifyNoMoreInteractions(storeRepository);
    }

    @DisplayName("update: changes 맵의 값들만 적용하고 markUpdatedBy 호출한다.")
    @Test
    void update_applies_changes_and_marks_updated() {
        // given
        Store store = mock(Store.class);

        // 병합 대상이 되는 기존 값 stubbing
        when(store.getLongitude()).thenReturn(126.0d);
        when(store.getClosingHours()).thenReturn(LocalTime.of(21, 0));

        UUID categoryId = UUID.randomUUID();
        Category category = mock(Category.class);
        when(categoryService.findByCategoryId(eq(categoryId))).thenReturn(category);

        Map<String, Object> changes = new HashMap<>();
        changes.put("storeName", "치킨천국");
        changes.put("latitude", 37.1234d);                // lon은 미포함 → 기존값으로 병합
        changes.put("openingHours", LocalTime.of(9, 0));  // close는 미포함 → 기존값으로 병합
        changes.put("minOrderAmount", 15000);
        changes.put("categoryId", categoryId);

        String userName = "anyUser";

        // when
        storeService.update(store, changes, userName);

        // then
        verify(store).changeStoreName("치킨천국");
        verify(store).changeMinOrderAmount(15000);
        verify(store).changeBusinessHours(LocalTime.of(9, 0), LocalTime.of(21, 0));
        verify(store).changeLocation(37.1234d, 126.0d);

        verify(categoryService).findByCategoryId(categoryId);
        verify(store).changeCategory(category);

        verify(store).markUpdatedBy(userName);
        verifyNoMoreInteractions(categoryService);
    }

    @DisplayName("delete: markDeletedBy 호출한다.")
    @Test
    void delete_marks_deleted() {
        Store store = mock(Store.class);
        String userName = "anyUser";

        storeService.delete(store, userName);

        verify(store).markDeletedBy(userName);
        verifyNoInteractions(storeRepository, categoryService);
    }

    @DisplayName("acceptFromConfirmed: Order.acceptFromConfirmed 메서드가 호출된다")
    @Test
    void acceptFromConfirmed_calls_order_method() {
        storeService.acceptFromConfirmed(order);
        verify(order).acceptFromConfirmed();
        verifyNoMoreInteractions(order);
    }

    @DisplayName("prepareFromAccepted: Order.prepareFromAccepted 메서드가 호출된다")
    @Test
    void prepareFromAccepted_calls_order_method() {
        storeService.prepareFromAccepted(order);
        verify(order).prepareFromAccepted();
        verifyNoMoreInteractions(order);
    }
}
