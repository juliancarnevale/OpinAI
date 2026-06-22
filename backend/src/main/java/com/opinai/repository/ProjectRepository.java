package com.opinai.repository;

import com.opinai.model.Project;
import com.opinai.model.User;
import com.opinai.service.dto.ProjectFeedbackCountDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByUser(User user);
    Optional<Project> findByIdAndUser(UUID id, User user);
    List<Project> findByUserId(UUID userId);
    
    long countByUser(User user);
    boolean existsByIdAndUser(UUID id, User user);

    @Query("SELECT new com.opinai.service.dto.ProjectFeedbackCountDto(p.id, p.name, p.description, p.createdAt, COUNT(f)) " +
           "FROM Project p " +
           "LEFT JOIN p.analyses a " +
           "LEFT JOIN a.feedbackItems f " +
           "WHERE p.user = :user " +
           "GROUP BY p.id, p.name, p.description, p.createdAt " +
           "ORDER BY p.createdAt DESC")
    List<ProjectFeedbackCountDto> findRecentProjectsWithFeedbackCount(@Param("user") User user, Pageable pageable);
}
