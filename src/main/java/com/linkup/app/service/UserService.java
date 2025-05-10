package com.linkup.app.service;

import com.linkup.app.dto.UserDTO;
import com.linkup.app.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

public interface UserService {
    // Original methods
    List<User> getAllUsers();
    Optional<User> getUserById(Long userId);
    Optional<User> getUserByEmail(String email);
    Optional<User> getUserByUsername(String username);
    User createUser(User user);
    User updateUser(Long userId, User userDetails);
    void deleteUser(Long userId);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    // New DTO methods
    List<UserDTO> getAllUsersDTO();
    Optional<UserDTO> getUserByIdDTO(Long userId);
    Optional<UserDTO> getUserByEmailDTO(String email);
    Optional<UserDTO> getUserByUsernameDTO(String username);
    UserDTO createUserDTO(User user);
    UserDTO updateUserDTO(Long userId, User userDetails);
    UserDTO convertToDTO(User user);
}