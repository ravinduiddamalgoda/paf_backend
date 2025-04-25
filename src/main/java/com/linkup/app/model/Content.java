package com.linkup.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String path;           // File storage path
    private String contentType;    // MIME type (e.g., "image/jpeg", "video/mp4")
    private String tag;            // For categorization

    // New fields for better media handling
    private String fileName;       // Original filename
    private Long fileSize;         // Size in bytes
    private String fileType;       // "image" or "video"
    private Integer duration;      // For videos, duration in seconds (null for images)

    @ManyToOne
    @JoinColumn(name = "postId", nullable = false)
    private Post post;
}