package com.linkup.app.service.impl;

import com.linkup.app.dto.UserDTO;
import com.linkup.app.model.User;
import com.linkup.app.repository.UserRepository;
import com.linkup.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUserName(username);
    }

    @Override
    public User createUser(User user) {
        // Validate user data
        if (user.getUserName() == null || user.getUserName().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        // Check if username or email already exists
        if (existsByUsername(user.getUserName())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long userId, User userDetails) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Update username if provided and not already taken by another user
        if (userDetails.getUserName() != null && !userDetails.getUserName().equals(existingUser.getUserName())) {
            if (existsByUsername(userDetails.getUserName())) {
                throw new IllegalArgumentException("Username already exists");
            }
            existingUser.setUserName(userDetails.getUserName());
        }

        // Update email if provided and not already taken by another user
        if (userDetails.getEmail() != null && !userDetails.getEmail().equals(existingUser.getEmail())) {
            if (existsByEmail(userDetails.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            existingUser.setEmail(userDetails.getEmail());
        }

        // Update password if provided
        if (userDetails.getPassword() != null && !userDetails.getPassword().trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        // Update OAuth provider information if provided
        if (userDetails.getProvider() != null) {
            existingUser.setProvider(userDetails.getProvider());
        }

        if (userDetails.getProviderId() != null) {
            existingUser.setProviderId(userDetails.getProviderId());
        }

        // Update enabled status
        existingUser.setEnabled(userDetails.isEnabled());

        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUserName(username);
    }

    @Override
    public Optional<User> findByProviderAndProviderId(String provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId);
    }

    // New DTO methods implementation
    @Override
    public UserDTO convertToDTO(User user) {
        if (user == null) return null;
        return new UserDTO(
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                user.getProvider(),
                user.getProviderId(),
                user.isEnabled()
        );
    }

    @Override
    public List<UserDTO> getAllUsersDTO() {
        return getAllUsers().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserDTO> getUserByIdDTO(Long userId) {
        return getUserById(userId)
                .map(this::convertToDTO);
    }

    @Override
    public Optional<UserDTO> getUserByEmailDTO(String email) {
        return getUserByEmail(email)
                .map(this::convertToDTO);
    }

    @Override
    public Optional<UserDTO> getUserByUsernameDTO(String username) {
        return getUserByUsername(username)
                .map(this::convertToDTO);
    }

    @Override
    public UserDTO createUserDTO(User user) {
        return convertToDTO(createUser(user));
    }

    @Override
    public UserDTO updateUserDTO(Long userId, User userDetails) {
        return convertToDTO(updateUser(userId, userDetails));
    }
}