package com.babgo.domain.ai.ReviewAnalysis;

import com.babgo.domain.store.Store;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "p_store_summary")
public class StoreSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "store_summary_id")
    private UUID storeSummaryId;

    @OneToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "summary_Text")
    private String summaryText;
}
