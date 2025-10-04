package com.babgo.repository.store;

import com.babgo.domain.store.Store;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreRepositoryImplTest {

    @Mock
    StoreJpaRepository storeJpaRepository;

    @InjectMocks
    StoreRepositoryImpl storeRepositoryImpl;

    @DisplayName("save: Store를 저장하고, 반환값을 그대로 돌려준다")
    @Test
    void save_success() {
        // given
        Store input = mock(Store.class);
        Store persisted = mock(Store.class);
        when(storeJpaRepository.save(same(input))).thenReturn(persisted);

        // when
        Store result = storeRepositoryImpl.save(input);

        // then
        assertThat(result).isSameAs(persisted);
        verify(storeJpaRepository, times(1)).save(same(input));
        verifyNoMoreInteractions(storeJpaRepository);
    }
}