package com.linkup.app.service;

import com.linkup.app.model.Content;
import com.linkup.app.model.Post;
import com.linkup.app.model.User;
import com.linkup.app.repository.PostRepository;
import com.linkup.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
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

    public Post createPost(Long userId, String description, List<MultipartFile> mediaFiles) throws IOException {
        // Check user exists
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("User not found");
        }

        // Validate number of files
        if (mediaFiles != null && mediaFiles.size() > 3) {
            throw new RuntimeException("Maximum 3 files allowed per post");
        }

        // Create post entity
        Post post = new Post();
        post.setUser(userOpt.get());
        post.setPostType("skill_sharing");
        post.setDescription(description);

        // Process and store media files
        List<Content> contentList = new ArrayList<>();

        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            for (MultipartFile file : mediaFiles) {
                String fileName = fileStorageService.storeFile(file);

                Content content = new Content();
                content.setPath(fileName);
                content.setContentType(file.getContentType());
                content.setFileName(file.getOriginalFilename());
                content.setFileSize(file.getSize());

                // Set file type (image or video)
                if (fileStorageService.isImageFile(file.getContentType())) {
                    content.setFileType("image");
                } else if (fileStorageService.isVideoFile(file.getContentType())) {
                    content.setFileType("video");
                    // TODO: Extract video duration here if possible
                    // For now, setting a default value
                    content.setDuration(0);
                }

                content.setPost(post);
                contentList.add(content);
            }
        }

        // Save post first
        Post savedPost = postRepository.save(post);

        // Then associate contents with saved post
        if (!contentList.isEmpty()) {
            for (Content content : contentList) {
                content.setPost(savedPost);
            }
            savedPost.getContents().addAll(contentList);
            savedPost = postRepository.save(savedPost);
        }

        return savedPost;
    }

    public List<Post> getPostsByUserId(Long userId) {
        return postRepository.findByUserUserIdOrderByPostIdDesc(userId);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByPostIdDesc();
    }

    public Optional<Post> getPostById(Long postId) {
        return postRepository.findById(postId);
    }
}