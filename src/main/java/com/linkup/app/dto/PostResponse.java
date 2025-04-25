package com.linkup.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long postId;
    private String description;
    private Long userId;
    private String userName;
    private List<ContentDTO> contents;
    private int likesCount;
    private int commentsCount;
}
