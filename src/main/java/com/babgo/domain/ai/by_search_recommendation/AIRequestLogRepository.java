package com.babgo.domain.ai.by_search_recommendation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AIRequestLogRepository extends JpaRepository<AIRequestLog, UUID> {
}
