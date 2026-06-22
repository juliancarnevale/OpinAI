package com.opinai.repository;

import com.opinai.model.Analysis;
import com.opinai.model.AnalysisStatus;
import com.opinai.model.Project;
import com.opinai.model.User;
import com.opinai.service.dto.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {
    
    List<Analysis> findByProjectOrderByCreatedAtDesc(Project project);

    @Query("SELECT a FROM Analysis a WHERE a.id = :id AND a.project.user = :user")
    Optional<Analysis> findByIdAndUser(@Param("id") UUID id, @Param("user") User user);

    @Modifying
    @Query("UPDATE Analysis a SET a.status = com.opinai.model.AnalysisStatus.PENDING, " +
           "a.overallSentiment = null, a.executiveSummary = null, a.keyIssues = null, " +
           "a.improvementOpportunities = null, a.sentimentDistribution = null " +
           "WHERE a.id = :id AND a.status = com.opinai.model.AnalysisStatus.FAILED AND a.project.user = :user")
    int reprocessAtomic(@Param("id") UUID id, @Param("user") User user);

    @Query("SELECT COUNT(a) FROM Analysis a WHERE a.project.user = :user")
    long countByUser(@Param("user") User user);

    @Query("SELECT COUNT(a) FROM Analysis a WHERE a.project.user = :user AND a.status = :status")
    long countByUserAndStatus(@Param("user") User user, @Param("status") AnalysisStatus status);

    @Query("SELECT COUNT(a) FROM Analysis a WHERE a.project.user = :user AND a.status IN :statuses")
    long countByUserAndStatusIn(@Param("user") User user, @Param("statuses") List<AnalysisStatus> statuses);

    @Query("SELECT a FROM Analysis a JOIN FETCH a.project WHERE a.project.user = :user ORDER BY a.createdAt DESC")
    List<Analysis> findRecentByUser(@Param("user") User user, Pageable pageable);

    // 1. Obtener la suma global de sentimientos con JSONB
    @Query(value = "SELECT " +
            "  COALESCE(SUM(CAST(a.sentiment_distribution->>'positive' AS integer)), 0) as positive, " +
            "  COALESCE(SUM(CAST(a.sentiment_distribution->>'negative' AS integer)), 0) as negative, " +
            "  COALESCE(SUM(CAST(a.sentiment_distribution->>'neutral' AS integer)), 0) as neutral " +
            "FROM analyses a " +
            "INNER JOIN projects p ON a.project_id = p.id " +
            "WHERE p.user_id = :userId " +
            "  AND a.status = 'COMPLETED' " +
            "  AND (cast(:projectId as uuid) IS NULL OR p.id = :projectId) " +
            "  AND (cast(:startDate as timestamp) IS NULL OR a.created_at >= :startDate) " +
            "  AND (cast(:endDate as timestamp) IS NULL OR a.created_at <= :endDate)", 
            nativeQuery = true)
    GlobalSentimentProjection findGlobalSentimentAggregate(
        @Param("userId") UUID userId,
        @Param("projectId") UUID projectId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // 2. Obtener tendencia temporal de sentimientos agrupados por fecha (CAST to date)
    @Query(value = "SELECT " +
            "  CAST(a.created_at AS date) as trendDate, " +
            "  COALESCE(SUM(CAST(a.sentiment_distribution->>'positive' AS integer)), 0) as positive, " +
            "  COALESCE(SUM(CAST(a.sentiment_distribution->>'negative' AS integer)), 0) as negative, " +
            "  COALESCE(SUM(CAST(a.sentiment_distribution->>'neutral' AS integer)), 0) as neutral, " +
            "  COUNT(a.id) as analysisCount " +
            "FROM analyses a " +
            "INNER JOIN projects p ON a.project_id = p.id " +
            "WHERE p.user_id = :userId " +
            "  AND a.status = 'COMPLETED' " +
            "  AND (cast(:projectId as uuid) IS NULL OR p.id = :projectId) " +
            "  AND (cast(:startDate as timestamp) IS NULL OR a.created_at >= :startDate) " +
            "  AND (cast(:endDate as timestamp) IS NULL OR a.created_at <= :endDate) " +
            "GROUP BY trendDate " +
            "ORDER BY trendDate ASC", 
            nativeQuery = true)
    List<SentimentTrendProjection> findSentimentTrend(
        @Param("userId") UUID userId,
        @Param("projectId") UUID projectId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // 3. Obtener comparación agregada de sentimientos por proyecto sin JOIN con feedback_items (Evita sobreconteo)
    @Query(value = "SELECT " +
            "  p.id as projectId, " +
            "  p.name as projectName, " +
            "  (COALESCE(SUM(CAST(a.sentiment_distribution->>'positive' AS integer)), 0) + " +
            "   COALESCE(SUM(CAST(a.sentiment_distribution->>'negative' AS integer)), 0) + " +
            "   COALESCE(SUM(CAST(a.sentiment_distribution->>'neutral' AS integer)), 0)) as totalFeedbacks, " +
            "  COALESCE(SUM(CAST(a.sentiment_distribution->>'positive' AS integer)), 0) as positive, " +
            "  COALESCE(SUM(CAST(a.sentiment_distribution->>'negative' AS integer)), 0) as negative, " +
            "  COALESCE(SUM(CAST(a.sentiment_distribution->>'neutral' AS integer)), 0) as neutral " +
            "FROM projects p " +
            "LEFT JOIN analyses a ON a.project_id = p.id AND a.status = 'COMPLETED' " +
            "  AND (cast(:startDate as timestamp) IS NULL OR a.created_at >= :startDate) " +
            "  AND (cast(:endDate as timestamp) IS NULL OR a.created_at <= :endDate) " +
            "WHERE p.user_id = :userId " +
            "  AND (cast(:projectId as uuid) IS NULL OR p.id = :projectId) " +
            "GROUP BY p.id, p.name " +
            "ORDER BY totalFeedbacks DESC", 
            nativeQuery = true)
    List<ProjectSentimentProjection> findProjectSentimentComparisons(
        @Param("userId") UUID userId,
        @Param("projectId") UUID projectId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // 4. Obtener los 10 Key Issues más frecuentes expandiendo el array JSONB
    @Query(value = "SELECT " +
            "  issue_text as issue, " +
            "  COUNT(*) as count " +
            "FROM ( " +
            "  SELECT jsonb_array_elements_text(a.key_issues) as issue_text " +
            "  FROM analyses a " +
            "  INNER JOIN projects p ON a.project_id = p.id " +
            "  WHERE p.user_id = :userId " +
            "    AND a.status = 'COMPLETED' " +
            "    AND (cast(:projectId as uuid) IS NULL OR p.id = :projectId) " +
            "    AND (cast(:startDate as timestamp) IS NULL OR a.created_at >= :startDate) " +
            "    AND (cast(:endDate as timestamp) IS NULL OR a.created_at <= :endDate) " +
            ") sub " +
            "GROUP BY issue_text " +
            "ORDER BY count DESC " +
            "LIMIT 10", 
            nativeQuery = true)
    List<KeyIssueProjection> findTopKeyIssues(
        @Param("userId") UUID userId,
        @Param("projectId") UUID projectId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // 5. Obtener las 10 mejores Oportunidades de Mejora expandiendo el array JSONB
    @Query(value = "SELECT " +
            "  opp_text as opportunity, " +
            "  COUNT(*) as count " +
            "FROM ( " +
            "  SELECT jsonb_array_elements_text(a.improvement_opportunities) as opp_text " +
            "  FROM analyses a " +
            "  INNER JOIN projects p ON a.project_id = p.id " +
            "  WHERE p.user_id = :userId " +
            "    AND a.status = 'COMPLETED' " +
            "    AND (cast(:projectId as uuid) IS NULL OR p.id = :projectId) " +
            "    AND (cast(:startDate as timestamp) IS NULL OR a.created_at >= :startDate) " +
            "    AND (cast(:endDate as timestamp) IS NULL OR a.created_at <= :endDate) " +
            ") sub " +
            "GROUP BY opp_text " +
            "ORDER BY count DESC " +
            "LIMIT 10", 
            nativeQuery = true)
    List<OpportunityProjection> findTopOpportunities(
        @Param("userId") UUID userId,
        @Param("projectId") UUID projectId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // 6. Única consulta de conteo consolidado para análisis
    @Query(value = "SELECT " +
            "  COUNT(a.id) as totalAnalyses, " +
            "  COALESCE(SUM(CASE WHEN a.status = 'COMPLETED' THEN 1 ELSE 0 END), 0) as completedAnalyses, " +
            "  COALESCE(SUM(CASE WHEN a.status IN ('PENDING', 'PROCESSING') THEN 1 ELSE 0 END), 0) as activeAnalyses " +
            "FROM analyses a " +
            "INNER JOIN projects p ON a.project_id = p.id " +
            "WHERE p.user_id = :userId " +
            "  AND (cast(:projectId as uuid) IS NULL OR p.id = :projectId)", 
            nativeQuery = true)
    AnalysisCountsProjection findAnalysisCounts(
        @Param("userId") UUID userId,
        @Param("projectId") UUID projectId
    );

    @Query("SELECT DISTINCT a FROM Analysis a " +
           "LEFT JOIN FETCH a.feedbackItems " +
           "WHERE a.project = :project AND a.status = :status")
    List<Analysis> findByProjectAndStatusWithFeedbacks(
            @Param("project") Project project, 
            @Param("status") AnalysisStatus status
    );
}
