package com.linkup.app.service;

import com.linkup.app.dto.CommentResponse;
import com.linkup.app.dto.PostResponse;
import com.linkup.app.model.Comment;
import com.linkup.app.model.Content;
import com.linkup.app.model.Post;
import com.linkup.app.model.User;
import com.linkup.app.repository.ContentRepository;
import com.linkup.app.repository.PostRepository;
import com.linkup.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ContentRepository contentRepository; // Add this if you don't have it already


    public Post createPost(Long userId, String description, List<MultipartFile> mediaFiles) throws IOException {
        // Validate user existence
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (mediaFiles != null && mediaFiles.size() > 3) {
            throw new IllegalArgumentException("Maximum 3 media files are allowed per post");
        }

        // Create new Post
        Post post = new Post();
        post.setUser(user);
        post.setPostType("skill_sharing");
        post.setDescription(description);
        post.setContents(new HashSet<>()); // Initialize empty set

        // Save Post early to get ID
        post = postRepository.save(post);

        // Process media files if any
        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            for (MultipartFile file : mediaFiles) {
                String fileName = fileStorageService.storeFile(file);

                // Create and save each Content separately
                Content content = new Content();
                content.setPath(fileName);
                content.setContentType(file.getContentType());
                content.setFileName(file.getOriginalFilename());
                content.setFileSize(file.getSize());
                content.setPost(post);

                // File type handling
                if (fileStorageService.isImageFile(file.getContentType())) {
                    content.setFileType("image");
                } else if (fileStorageService.isVideoFile(file.getContentType())) {
                    content.setFileType("video");
                    content.setDuration(0); // Placeholder for video duration
                } else {
                    content.setFileType("unknown");
                }

                // Save each content object individually
                contentRepository.save(content);
            }
        }

        // Re-fetch the post to ensure we have all associations loaded
        return postRepository.findById(post.getPostId())
                .orElseThrow(() -> new RuntimeException("Failed to retrieve post after saving"));
    }




    public List<Post> getPostsByUserId(Long userId) {
        return postRepository.findByUserUserIdOrderByPostIdDesc(userId);
    }

    public List<PostResponse> getAllPosts() {
        List<Post> posts = postRepository.findAllByOrderByPostIdDesc();
        List<PostResponse> postResponses = new ArrayList<>();
        for (Post post : posts) {
            PostResponse postResponse = new PostResponse();

            postResponse.setPostId(post.getPostId());
            postResponse.setDescription(post.getDescription());
            postResponse.setPostType(post.getPostType());
            postResponse.setCreatedAt(post.getCreatedAt());

            // User Info
            if (post.getUser() != null) {
                postResponse.setUserId(post.getUser().getUserId());
                postResponse.setUserName(post.getUser().getUserName());
            }
            // Contents
            postResponse.setContents(post.getContents());
            // Comments & Likes Count
            postResponse.setCommentsCount(post.getComments().size());
            List<CommentResponse> commentResponses = new ArrayList<>();
            for (Comment comment : post.getComments()) {
                CommentResponse commentResponse = new CommentResponse();
                commentResponse.setCommentId(comment.getCommentId());
                commentResponse.setCreatedAt(comment.getCreatedAt());
                commentResponse.setPostId(comment.getPost().getPostId());
                commentResponse.setContent(comment.getContent());
                //commentResponse.setParentCommentId(comment.getParentComment().getCommentId());
                commentResponse.setUserId(comment.getUser().getUserId());
                commentResponse.setUserName(comment.getUser().getUserName());

                commentResponses.add(commentResponse);
            }
            postResponse.setComments(commentResponses);
            postResponse.setLikesCount(post.getLikes().size());
            postResponses.add(postResponse);
        }
        return postResponses;
    }

    public Optional<Post> getPostById(Long postId) {
        return postRepository.findById(postId);
    }

    public boolean deletePost(Long postId, Long userId) {
        Optional<Post> postOpt = postRepository.findById(postId);

        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            if (!post.getUser().getUserId().equals(userId)) {
                throw new SecurityException("Unauthorized: You can only delete your own posts");
            }
            postRepository.delete(post);
            return true;
        } else {
            return false;
        }
    }

    public Post updatePostDescription(Long postId, Long userId, String newDescription) {
        Optional<Post> postOpt = postRepository.findById(postId);

        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            if (!post.getUser().getUserId().equals(userId)) {
                throw new SecurityException("Unauthorized: You can only update your own posts");
            }
            post.setDescription(newDescription);
            return postRepository.save(post);
        } else {
            throw new RuntimeException("Post not found with ID: " + postId);
        }
    }


}