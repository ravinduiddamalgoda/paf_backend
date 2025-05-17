package com.linkup.app.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

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
    @JoinColumn(name = "post_id")
    @JsonBackReference
    private Post post;

    /*
     * Modified equals and hashCode methods to avoid the issue where
     * new Content objects with null IDs are considered equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Content content = (Content) o;

        // If both have IDs, compare by ID
        if (id != null && content.id != null) {
            return Objects.equals(id, content.id);
        }

        // If either has no ID, compare by values that should make it unique
        return Objects.equals(path, content.path) &&
                Objects.equals(fileName, content.fileName) &&
                Objects.equals(fileSize, content.fileSize);
    }

    @Override
    public int hashCode() {
        // If ID exists, use it for hash
        if (id != null) {
            return Objects.hash(id);
        }
        // Otherwise use combination of fields that should make it unique
        return Objects.hash(path, fileName, fileSize);
    }
}