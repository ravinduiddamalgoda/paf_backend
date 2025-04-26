package com.linkup.app.repository;

import com.linkup.app.model.LearningPathContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningPathContentRepository extends JpaRepository<LearningPathContent, Long> {
    List<LearningPathContent> findByLearningPathId(Long learningPathId);

    @Query("SELECT lpc FROM LearningPathContent lpc WHERE lpc.learningPath.id = :learningPathId ORDER BY lpc.ordinal ASC")
    List<LearningPathContent> findByLearningPathIdOrderByOrdinalAsc(Long learningPathId);

    int countByLearningPathIdAndIsCompletedTrue(Long learningPathId);

    int countByLearningPathId(Long learningPathId);
}