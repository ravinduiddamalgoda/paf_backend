package com.linkup.app.service;

import com.linkup.app.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

//@Service
public interface UserService {

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
}