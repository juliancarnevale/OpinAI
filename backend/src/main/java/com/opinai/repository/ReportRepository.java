package com.opinai.repository;

import com.opinai.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    @Query("SELECT r FROM Report r JOIN r.project p JOIN p.user u WHERE r.id = :id AND u.email = :email")
    Optional<Report> findByIdAndProjectUserEmail(@Param("id") UUID id, @Param("email") String email);

    @Query("SELECT r FROM Report r JOIN r.project p JOIN p.user u WHERE p.id = :projectId AND u.email = :email ORDER BY r.createdAt DESC")
    List<Report> findByProjectIdAndProjectUserEmailOrderByCreatedAtDesc(@Param("projectId") UUID projectId, @Param("email") String email);

    @Query("SELECT r.storageKey FROM Report r WHERE r.project.id = :projectId")
    List<String> findStorageKeysByProjectId(@Param("projectId") UUID projectId);
}
