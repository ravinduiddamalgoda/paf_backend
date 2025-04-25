package com.linkup.app.dto;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentDTO {
    private Long id;
    private String path;
    private String contentType;
    private String fileType; // "image" or "video"
    private Integer duration; // For videos
}