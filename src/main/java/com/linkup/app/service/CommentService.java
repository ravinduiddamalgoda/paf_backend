package com.linkup.app.service;

import com.linkup.app.dto.CommentResponse;
import com.linkup.app.model.Comment;
import com.linkup.app.model.Post;
import com.linkup.app.model.User;
import com.linkup.app.repository.CommentRepository;
import com.linkup.app.repository.PostRepository;
import com.linkup.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Transactional
    public Comment addComment(Long userId, Long postId, String content, Long parentCommentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    public List<CommentResponse> getPostComments(Long postId) {
        // Get all top-level comments
        List<Comment> topLevelComments = commentRepository.findByPostPostIdAndParentCommentIsNullOrderByCreatedAtDesc(postId);

        // Convert to response DTOs with replies
        return topLevelComments.stream()
                .map(this::convertToCommentResponseWithReplies)
                .collect(Collectors.toList());
    }

    private CommentResponse convertToCommentResponseWithReplies(Comment comment) {
        CommentResponse response = new CommentResponse(
                comment.getCommentId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUser().getUserId(),
                comment.getUser().getUserName(),
                comment.getPost().getPostId(),
                new ArrayList<>()
        );

        // Get replies and convert them recursively
        List<Comment> replies = commentRepository.findByParentCommentCommentIdOrderByCreatedAtAsc(comment.getCommentId());
        response.setReplies(
                replies.stream()
                        .map(this::convertToCommentResponseWithReplies)
                        .collect(Collectors.toList())
        );

        return response;
    }

    public Optional<Comment> getCommentById(Long commentId) {
        return commentRepository.findById(commentId);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Check if the user is the owner of the comment
        if (!comment.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

    public int getCommentsCount(Long postId) {
        return commentRepository.countByPostPostId(postId);
    }

    @Transactional
    public Comment updateComment(Long commentId, Long userId, String newContent) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to update this comment");
        }

        comment.setContent(newContent);
        return commentRepository.save(comment);
    }
}