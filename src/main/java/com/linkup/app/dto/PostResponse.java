package com.linkup.app.dto;

import com.linkup.app.model.Comment;
import com.linkup.app.model.Content;
import com.linkup.app.model.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long postId;
    private String postType;
    private LocalDateTime createdAt;
    private String description;
    private Long userId;
    private String userName;
    private Set<Content> contents;
    private int likesCount;
    private int commentsCount;
    private List<CommentResponse> comments;
}
