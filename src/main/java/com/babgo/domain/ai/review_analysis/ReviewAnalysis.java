package com.babgo.domain.ai.ReviewAnalysis;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "p_review_nalysis")
public class ReviewAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "review_analysis_id")
    private UUID reviewAnalysisId;

    @OneToOne
    @JoinColumn(name = "review_id")
    private Review review;

    private double sentimentScore; // -N..+N (룰 기반 또는 모델 스케일)
    private String keywords; // comma separated
    private double fakeScore; // 0..1, 높을수록 허위 가능성
}
