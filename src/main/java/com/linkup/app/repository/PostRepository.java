package com.linkup.app.repository;

import com.linkup.app.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserUserIdOrderByPostIdDesc(Long userId);
    List<Post> findAllByOrderByPostIdDesc();
}