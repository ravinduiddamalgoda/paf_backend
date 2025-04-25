package com.linkup.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/websocket")
public class WebSocketTestController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/test")
    public ResponseEntity<?> testWebSocket(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // Send a test message to the authenticated user
            Map<String, Object> testMessage = new HashMap<>();
            testMessage.put("message", "WebSocket connection test successful");
            testMessage.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSendToUser(
                    authentication.getName(),
                    "/queue/test",
                    testMessage
            );

            return ResponseEntity.ok("Test message sent");
        }

        return ResponseEntity.badRequest().body("User not authenticated");
    }
}