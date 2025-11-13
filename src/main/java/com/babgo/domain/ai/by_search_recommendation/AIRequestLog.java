package com.babgo.domain.ai.by_search_recommendation;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "p_ai_request_logs")
public class AIRequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="ai_request_log_id")
    private UUID aiRequestLogId;

    private String prompt;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Column(name="cretaed_at")
    private LocalDateTime createdAt;
}
