package com.linkup.app.repository;

import com.linkup.app.model.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    /**
     * Find all content associated with a specific post
     * @param postId The ID of the post
     * @return List of content objects
     */
    List<Content> findByPostPostId(Long postId);

    /**
     * Delete all content associated with a post
     * @param postId The ID of the post
     */
    void deleteByPostPostId(Long postId);

    /**
     * Find content by its file path
     * @param path The file path
     * @return Content object if found
     */
    Content findByPath(String path);

    /**
     * Count the number of content items for a specific post
     * @param postId The ID of the post
     * @return Count of content items
     */
    long countByPostPostId(Long postId);

    /**
     * Find content by type for a specific post
     * @param postId The ID of the post
     * @param fileType The file type (e.g., "image", "video")
     * @return List of matching content
     */
    List<Content> findByPostPostIdAndFileType(Long postId, String fileType);
}