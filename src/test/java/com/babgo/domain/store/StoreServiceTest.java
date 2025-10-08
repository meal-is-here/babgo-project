package com.babgo.domain.store;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}