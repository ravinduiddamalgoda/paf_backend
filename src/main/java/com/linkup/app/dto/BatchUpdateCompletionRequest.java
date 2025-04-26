package com.linkup.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchUpdateCompletionRequest {
    private List<Long> contentIds;
    private Boolean isCompleted;
}