package com.linkup.app.controller;

import com.linkup.app.dto.CommentRequest;
import com.linkup.app.dto.CommentResponse;
import com.linkup.app.dto.UpdateCommentRequest;
import com.linkup.app.model.Comment;
import com.linkup.app.model.User;
import com.linkup.app.repository.UserRepository;
import com.linkup.app.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> addComment(@RequestBody CommentRequest commentRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOpt.get();
        Comment comment = commentService.addComment(
                user.getUserId(),
                commentRequest.getPostId(),
                commentRequest.getContent()
                // Remove parent comment ID parameter
        );

        CommentResponse response = new CommentResponse(
                comment.getCommentId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUser().getUserId(),
                comment.getUser().getUserName(),
                comment.getPost().getPostId(),
                List.of() // Empty replies list
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/post/{postId}")
    public ResponseEntity<?> getPostComments(@PathVariable Long postId) {
        List<CommentResponse> comments = commentService.getPostComments(postId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<?> getComment(@PathVariable Long commentId) {
        return commentService.getCommentById(commentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        try {
            commentService.deleteComment(commentId, userOpt.get().getUserId());
            return ResponseEntity.ok().body("Comment deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/post/{postId}/count")
    public ResponseEntity<?> getCommentsCount(@PathVariable Long postId) {
        int count = commentService.getCommentsCount(postId);
        return ResponseEntity.ok().body(count);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long commentId,
                                           @RequestBody UpdateCommentRequest updateRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        try {
            Comment updatedComment = commentService.updateComment(
                    commentId,
                    userOpt.get().getUserId(),
                    updateRequest.getContent()
            );

            CommentResponse response = new CommentResponse(
                    updatedComment.getCommentId(),
                    updatedComment.getContent(),
                    updatedComment.getCreatedAt(),
                    updatedComment.getUser().getUserId(),
                    updatedComment.getUser().getUserName(),
                    updatedComment.getPost().getPostId(),
                    List.of() // No replies here
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}