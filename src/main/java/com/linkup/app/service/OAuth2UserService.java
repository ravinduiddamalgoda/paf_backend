package com.linkup.app.service;

import com.linkup.app.model.User;
import com.linkup.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Extract provider details
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oAuth2User.getAttribute("sub"); // For Google

        // Different providers use different attribute keys
        if (providerId == null) {
            providerId = oAuth2User.getAttribute("id"); // For Facebook
        }

        // Extract common user info
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Check if user exists
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            // Update existing user with OAuth info if not already set
            User user = existingUser.get();
            if (user.getProvider() == null) {
                user.setProvider(provider);
                user.setProviderId(providerId);
                userRepository.save(user);
            }
        } else {
            // Create new user
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUserName(generateUniqueUsername(name));
            newUser.setPassword(""); // Not used for OAuth users
            newUser.setProvider(provider);
            newUser.setProviderId(providerId);
            newUser.setEnabled(true);
            userRepository.save(newUser);
        }

        return oAuth2User;
    }

    private String generateUniqueUsername(String name) {
        // Remove spaces and make lowercase
        String baseUsername = name.replaceAll("\\s+", "").toLowerCase();

        // Check if username exists
        if (!userRepository.existsByUserName(baseUsername)) {
            return baseUsername;
        }

        // Add random number until unique
        int counter = 1;
        String username = baseUsername + counter;
        while (userRepository.existsByUserName(username)) {
            counter++;
            username = baseUsername + counter;
        }

        return username;
    }
}