package com.babgo.domain.ai.review_analysis;

import com.babgo.domain.review.Review;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "p_review_analysis")
public class ReviewAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "review_analysis_id")
    private UUID reviewAnalysisId;

    @Setter
    @OneToOne
    @JoinColumn(name = "review_id")
    private Review review;

    @Setter
    private double sentimentScore; // -N..+N (룰 기반 또는 모델 스케일)

    @Setter
    private String keywords; // comma separated

    @Setter
    private double fakeScore; // 0..1, 높을수록 허위 가능성
}
