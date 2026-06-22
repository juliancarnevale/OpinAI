package com.opinai.repository;

import com.opinai.model.FeedbackItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FeedbackItemRepository extends JpaRepository<FeedbackItem, UUID> {
    Page<FeedbackItem> findByAnalysisId(UUID analysisId, Pageable pageable);
    long countByAnalysisProject(com.opinai.model.Project project);
}
