package com.linkup.app.controller;

import com.linkup.app.dto.PostCreateRequest;
import com.linkup.app.model.Post;
import com.linkup.app.model.User;
import com.linkup.app.repository.UserRepository;
import com.linkup.app.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createPost(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestPart("description") String description,
            @RequestPart(value = "media", required = false) List<MultipartFile> mediaFiles) {

        try {
            // Get authenticated user ID
            Map<String, Object> attributes = principal.getAttributes();
            String email = (String) attributes.get("email");

            // Lookup user by email from OAuth info
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Validate number of files
            if (mediaFiles != null && mediaFiles.size() > 3) {
                return ResponseEntity.badRequest().body("Maximum 3 files allowed per post");
            }

            // Create post
            Post post = postService.createPost(user.getUserId(), description, mediaFiles);

            return ResponseEntity.status(HttpStatus.CREATED).body(post);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create post: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(@PathVariable Long postId) {
        return postService.getPostById(postId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Post>> getPostsByUserId(@PathVariable Long userId) {
        List<Post> posts = postService.getPostsByUserId(userId);
        return ResponseEntity.ok(posts);
    }
}