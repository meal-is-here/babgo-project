package com.babgo.repository.ai.store_summary;

import com.babgo.domain.ai.store_summary.StoreSummary;
import com.babgo.domain.ai.store_summary.StoreSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreSummaryRepositoryImpl implements StoreSummaryRepository {

    private final StoreSummaryJpaRepository StoreSummaryJpaRepository;

    @Override
    @Transactional
    public StoreSummary save(StoreSummary storeSummary) {
        return StoreSummaryJpaRepository.save(storeSummary);
    }
}
