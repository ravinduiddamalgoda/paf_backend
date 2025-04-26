package com.linkup.app.repository;

import com.linkup.app.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    List<Like> findByPostPostId(Long postId);
    Optional<Like> findByUserUserIdAndPostPostId(Long userId, Long postId);
    boolean existsByUserUserIdAndPostPostId(Long userId, Long postId);
    void deleteByUserUserIdAndPostPostId(Long userId, Long postId);
    int countByPostPostId(Long postId);
}