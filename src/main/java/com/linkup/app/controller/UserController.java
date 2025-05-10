package com.linkup.app.controller;

import com.linkup.app.dto.UserDTO;
import com.linkup.app.model.User;
import com.linkup.app.repository.UserRepository;
import com.linkup.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Simple health check endpoint
     */
    @GetMapping("/hello")
    public String getHello() {
        return "Hello from LinkUp API";
    }

    /**
     * Get all users
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsersDTO();
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        return userService.getUserByIdDTO(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get user by email
     */
    @GetMapping("/by-email")
    public ResponseEntity<UserDTO> getUserByEmail(@RequestParam String email) {
        return userService.getUserByEmailDTO(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get user by username
     */
    @GetMapping("/by-username")
    public ResponseEntity<UserDTO> getUserByUsername(@RequestParam String username) {
        return userService.getUserByUsernameDTO(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new user
     */
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            UserDTO createdUser = userService.createUserDTO(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update an existing user
     */
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody User userDetails) {
        try {
            UserDTO updatedUser = userService.updateUserDTO(userId, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete a user
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Check if email exists
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * Check if username exists
     */
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsernameExists(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * Find user by OAuth provider details
     */
    @GetMapping("/oauth")
    public ResponseEntity<UserDTO> findByProviderAndProviderId(
            @RequestParam String provider,
            @RequestParam String providerId) {
        return userService.findByProviderAndProviderId(provider, providerId)
                .map(user -> ResponseEntity.ok(userService.convertToDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String email;

        // Extract email from principal
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else if (principal instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unsupported authentication principal");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found with email: " + email);
        }

        User user = userOpt.get();
        UserDTO userDTO = userService.convertToDTO(user);

        Map<String, Object> response = new HashMap<>();
        response.put("user", userDTO);
        response.put("authenticated", true);

        return ResponseEntity.ok(response);
    }


}