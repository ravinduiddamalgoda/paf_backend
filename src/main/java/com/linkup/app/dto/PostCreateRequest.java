package com.linkup.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {
    private String description;
    // Media files will be handled as MultipartFile in the controller
}