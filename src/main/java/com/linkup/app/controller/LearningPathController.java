package com.linkup.app.controller;

import com.linkup.app.dto.BatchUpdateCompletionRequest;
import com.linkup.app.dto.LearningPathContentDTO;
import com.linkup.app.dto.LearningPathDTO;
import com.linkup.app.dto.UpdateCompletionRequest;
import com.linkup.app.model.User;
import com.linkup.app.repository.UserRepository;
import com.linkup.app.service.LearningPathService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/learning-paths")
public class LearningPathController {

    private static final Logger logger = Logger.getLogger(LearningPathController.class.getName());

    @Autowired
    private LearningPathService learningPathService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getUserLearningPaths(@AuthenticationPrincipal Object principal) {
        try {
            String email = getUserEmail(principal);
            User user = getUserByEmail(email);

            List<LearningPathDTO> learningPaths = learningPathService.getLearningPathsByUserId(user.getUserId());
            return ResponseEntity.status(HttpStatus.OK).body(learningPaths);
        } catch (Exception e) {
            logger.severe("Error retrieving learning paths: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving learning paths: " + e.getMessage());
        }
    }

    @GetMapping("/{pathId}")
    public ResponseEntity<?> getLearningPath(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long pathId) {
        try {
            String email = getUserEmail(principal);
            User user = getUserByEmail(email);

            LearningPathDTO learningPath = learningPathService.getLearningPathById(pathId);

            // Check if the learning path belongs to the authenticated user
            if (!learningPath.getUserId().equals(user.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Unauthorized: You can only view your own learning paths");
            }

            return ResponseEntity.ok(learningPath);
        } catch (Exception e) {
            logger.severe("Error retrieving learning path: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving learning path: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createLearningPath(
            @AuthenticationPrincipal Object principal,
            @RequestBody Map<String, Object> requestBody) {
        try {
            String email = getUserEmail(principal);
            User user = getUserByEmail(email);

            String name = (String) requestBody.get("name");
            Integer tag = (Integer) requestBody.get("tag");

            // Fix the conversion from LinkedHashMap to LearningPathContentDTO
            List<Map<String, Object>> contentMaps = (List<Map<String, Object>>) requestBody.get("contents");
            List<LearningPathContentDTO> contents = new ArrayList<>();

            if (contentMaps != null) {
                for (Map<String, Object> contentMap : contentMaps) {
                    LearningPathContentDTO contentDto = new LearningPathContentDTO();
                    contentDto.setContentTitle((String) contentMap.get("contentTitle"));
                    contentDto.setContentDescription((String) contentMap.get("contentDescription"));
                    contentDto.setContentUrl((String) contentMap.get("contentUrl"));
                    contentDto.setOrdinal((Integer) contentMap.get("ordinal"));
                    contents.add(contentDto);
                }
            }

            LearningPathDTO learningPath = learningPathService.createLearningPath(
                    user.getUserId(), name, tag, contents);

            return ResponseEntity.status(HttpStatus.CREATED).body(learningPath);
        } catch (Exception e) {
            logger.severe("Error creating learning path: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating learning path: " + e.getMessage());
        }
    }

    @PutMapping("/{pathId}")
    public ResponseEntity<?> updateLearningPath(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long pathId,
            @RequestBody Map<String, Object> requestBody) {
        try {
            String email = getUserEmail(principal);
            User user = getUserByEmail(email);

            // First check if the learning path belongs to the authenticated user
            LearningPathDTO existingPath = learningPathService.getLearningPathById(pathId);
            if (!existingPath.getUserId().equals(user.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Unauthorized: You can only update your own learning paths");
            }

            String name = (String) requestBody.get("name");
            Integer tag = (Integer) requestBody.get("tag");

            LearningPathDTO updatedPath = learningPathService.updateLearningPath(pathId, name, tag);

            return ResponseEntity.ok(updatedPath);
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(se.getMessage());
        } catch (Exception e) {
            logger.severe("Error updating learning path: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating learning path: " + e.getMessage());
        }
    }

    @DeleteMapping("/{pathId}")
    public ResponseEntity<?> deleteLearningPath(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long pathId) {
        try {
            String email = getUserEmail(principal);
            User user = getUserByEmail(email);

            learningPathService.deleteLearningPath(pathId, user.getUserId());

            return ResponseEntity.ok("Learning path deleted successfully");
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(se.getMessage());
        } catch (Exception e) {
            logger.severe("Error deleting learning path: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting learning path: " + e.getMessage());
        }
    }

    @PostMapping("/{pathId}/contents")
    public ResponseEntity<?> addContent(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long pathId,
            @RequestBody LearningPathContentDTO contentDto) {
        try {
            String email = getUserEmail(principal);
            User user = getUserByEmail(email);

            // First check if the learning path belongs to the authenticated user
            LearningPathDTO existingPath = learningPathService.getLearningPathById(pathId);
            if (!existingPath.getUserId().equals(user.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Unauthorized: You can only add content to your own learning paths");
            }

            LearningPathContentDTO createdContent = learningPathService.addContent(pathId, contentDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdContent);
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(se.getMessage());
        } catch (Exception e) {
            logger.severe("Error adding content: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding content: " + e.getMessage());
        }
    }

    // This is the endpoint for updating completion percentage
    @PutMapping("/contents/{contentId}/completion")
    public ResponseEntity<?> updateCompletion(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long contentId,
            @RequestBody UpdateCompletionRequest request) {
        try {
            String email = getUserEmail(principal);
            User user = getUserByEmail(email);

            LearningPathContentDTO updatedContent = learningPathService.updateContentCompletion(
                    contentId, request.getIsCompleted(), user.getUserId());

            return ResponseEntity.ok(updatedContent);
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(se.getMessage());
        } catch (Exception e) {
            logger.severe("Error updating completion status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating completion status: " + e.getMessage());
        }
    }

    @DeleteMapping("/contents/{contentId}")
    public ResponseEntity<?> deleteContent(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long contentId) {
        try {
            String email = getUserEmail(principal);
            User user = getUserByEmail(email);

            learningPathService.deleteContent(contentId, user.getUserId());

            return ResponseEntity.ok("Content deleted successfully");
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(se.getMessage());
        } catch (Exception e) {
            logger.severe("Error deleting content: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting content: " + e.getMessage());
        }
    }

    // Endpoint to get the completion percentage for a learning path
    @GetMapping("/{pathId}/completion")
    public ResponseEntity<?> getCompletionPercentage(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long pathId) {
        try {
            String email = getUserEmail(principal);
            User user = getUserByEmail(email);

            // First check if the learning path belongs to the authenticated user
            LearningPathDTO existingPath = learningPathService.getLearningPathById(pathId);
            if (!existingPath.getUserId().equals(user.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Unauthorized: You can only view your own learning paths");
            }

            int percentage = learningPathService.calculateCompletionPercentage(pathId);

            return ResponseEntity.ok(Map.of(
                    "pathId", pathId,
                    "completionPercentage", percentage
            ));
        } catch (Exception e) {
            logger.severe("Error getting completion percentage: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting completion percentage: " + e.getMessage());
        }
    }

    // Endpoint for batch updating content completion status
    @PutMapping("/contents/batch-update")
    public ResponseEntity<?> batchUpdateCompletion(
            @AuthenticationPrincipal Object principal,
            @RequestBody BatchUpdateCompletionRequest request) {
        try {
            String email = getUserEmail(principal);
            User user = getUserByEmail(email);

            List<LearningPathContentDTO> updatedContents = learningPathService.batchUpdateContentCompletion(
                    request.getContentIds(), request.getIsCompleted(), user.getUserId());

            return ResponseEntity.ok(updatedContents);
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(se.getMessage());
        } catch (Exception e) {
            logger.severe("Error batch updating completion status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error batch updating completion status: " + e.getMessage());
        }
    }

    // Endpoint to get all learning paths with completion statistics
    @GetMapping("/stats")
    public ResponseEntity<?> getUserLearningPathStats(@AuthenticationPrincipal Object principal) {
        try {
            String email = getUserEmail(principal);
            User user = getUserByEmail(email);

            List<LearningPathDTO> learningPaths = learningPathService.getLearningPathsByUserId(user.getUserId());

            // Transform to include only statistics
            List<Map<String, Object>> pathStats = learningPaths.stream()
                    .map(path -> {
                        Map<String, Object> statsMap = new HashMap<>();
                        statsMap.put("pathId", path.getId());
                        statsMap.put("name", path.getName());
                        statsMap.put("tag", path.getTag() != null ? path.getTag() : 0);
                        statsMap.put("completedCount", path.getCompletedCount());
                        statsMap.put("totalContentCount", path.getTotalContentCount());

                        int completionPercentage = 0;
                        if (path.getTotalContentCount() > 0) {
                            completionPercentage = (path.getCompletedCount() * 100) / path.getTotalContentCount();
                        }
                        statsMap.put("completionPercentage", completionPercentage);

                        return statsMap;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(pathStats);
        } catch (Exception e) {
            logger.severe("Error retrieving learning path stats: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving learning path stats: " + e.getMessage());
        }
    }

    // Helper methods
    private String getUserEmail(Object principal) {
        if (principal instanceof OAuth2User oauth2User) {
            return (String) oauth2User.getAttributes().get("email");
        } else if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        } else {
            throw new IllegalArgumentException("Unauthorized: Invalid authentication");
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));
    }
}