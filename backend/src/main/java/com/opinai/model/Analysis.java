package com.opinai.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "analyses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Analysis extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "title", nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private AnalysisStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_sentiment", length = 50)
    private SentimentType overallSentiment;

    @Column(name = "executive_summary", columnDefinition = "TEXT")
    private String executiveSummary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "key_issues")
    private List<String> keyIssues;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "improvement_opportunities")
    private List<String> improvementOpportunities;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sentiment_distribution")
    private SentimentDistribution sentimentDistribution;

    @Builder.Default
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedbackItem> feedbackItems = new ArrayList<>();
}
