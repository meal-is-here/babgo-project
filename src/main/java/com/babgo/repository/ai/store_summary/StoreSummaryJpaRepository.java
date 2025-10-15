package com.babgo.repository.ai.store_summary;

import com.babgo.domain.ai.store_summary.StoreSummary;
import com.babgo.domain.ai.store_summary.StoreSummaryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreSummaryJpaRepository extends JpaRepository<StoreSummary, UUID> {
}
