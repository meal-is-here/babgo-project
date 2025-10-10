package com.babgo.domain.store;

import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    StoreRepository storeRepository;

    @InjectMocks
    StoreService storeService;


    @DisplayName("Store객체를 저장하고, 리포지토리의 반환값을 그대로 돌려준다.")
    @Test
    void create_success() {
        // given
        Store input = mock(Store.class);
        Store persisted = mock(Store.class);
        when(storeRepository.save(same(input))).thenReturn(persisted);
        // when
        Store store = storeService.create(input);
        // then
        Assertions.assertThat(store).isEqualTo(persisted);
        verify(storeRepository, times(1)).save(same(input));
    }

    @DisplayName("storeId로 가게를 조회하고, 리포지토리의 반환값을 그대로 돌려준다.")
    @Test
    void findByStoreId_success() {
        // given
        UUID storeId = UUID.randomUUID();
        Store found = mock(Store.class);
        when(storeRepository.findByStoreId(same(storeId))).thenReturn(Optional.of(found));

        // when
        Store result = storeService.findByStoreId(storeId);

        // then
        Assertions.assertThat(result).isSameAs(found);
        verify(storeRepository, times(1)).findByStoreId(same(storeId));
        verifyNoMoreInteractions(storeRepository);
    }

    @DisplayName("storeId로 가게 조회 시 존재하지 않으면 CustomException(NOT_FOUND)을 던진다.")
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
}