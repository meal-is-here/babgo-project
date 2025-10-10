package com.babgo.domain.ai.store_summary;

import com.babgo.domain.store.Store;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Setter
    @OneToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @Setter
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "summary_Text")
    private String summaryText;
}
