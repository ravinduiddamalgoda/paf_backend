package com.linkup.app.controller;

import com.linkup.app.dto.ChatNotification;
import com.linkup.app.dto.MessageRequest;
import com.linkup.app.dto.MessageResponse;
import com.linkup.app.model.Message;
import com.linkup.app.model.User;
import com.linkup.app.repository.UserRepository;
import com.linkup.app.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;

@Controller
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserRepository userRepository;


    @MessageMapping("/chat")
    public void processMessage(@Payload MessageRequest messageRequest) {
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            logger.error("Unauthenticated or invalid user tried to send a message.");
            return; // Stop processing if no valid authentication
        }

        String email = auth.getName();

        Optional<User> senderOpt = userRepository.findByEmail(email);

        if (senderOpt.isPresent()) {
            User sender = senderOpt.get();
            Long senderId = sender.getUserId();

            // Save the message
            Message savedMessage = messageService.saveMessage(
                    senderId,
                    messageRequest.getReceiverId(),
                    messageRequest.getContent()
            );

            // Create response DTO
            MessageResponse response = new MessageResponse(
                    savedMessage.getMessageId(),
                    savedMessage.getSender().getUserId(),
                    savedMessage.getSender().getUserName(),
                    savedMessage.getReceiver().getUserId(),
                    savedMessage.getReceiver().getUserName(),
                    savedMessage.getContent(),
                    savedMessage.getTimestamp()
            );

            // Send message to recipient
            messagingTemplate.convertAndSendToUser(
                    savedMessage.getReceiver().getUserId().toString(),
                    "/queue/messages",
                    response
            );

            // Send notification to recipient
            messagingTemplate.convertAndSendToUser(
                    savedMessage.getReceiver().getUserId().toString(),
                    "/queue/notification",
                    new ChatNotification(
                            senderId,
                            sender.getUserName(),
                            "New message from " + sender.getUserName()
                    )
            );

            logger.info("Message sent from {} to {}", senderId, messageRequest.getReceiverId());
        } else {
            logger.error("Sender not found for email: {}", email);
        }
    }


    @MessageMapping("/message/delivered")
    public void markAsDelivered(@Payload Long messageId) {
        messageService.markAsDelivered(messageId);
    }

    @MessageMapping("/message/read")
    public void markAsRead(@Payload Long messageId) {
        messageService.markAsRead(messageId);
    }

    // REST endpoints for message history
    @GetMapping("/api/messages/{userId}")
    @ResponseBody
    public List<MessageResponse> getConversation(@PathVariable Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Optional<User> currentUserOpt = userRepository.findByEmail(email);

        if (currentUserOpt.isPresent()) {
            User currentUser = currentUserOpt.get();
            return messageService.getConversation(currentUser.getUserId(), userId);
        }

        return List.of(); // Empty list if user not found
    }

    @GetMapping("/api/messages/users")
    @ResponseBody
    public List<User> getChatUsers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Optional<User> currentUserOpt = userRepository.findByEmail(email);

        if (currentUserOpt.isPresent()) {
            User currentUser = currentUserOpt.get();
            return messageService.getChatUsers(currentUser.getUserId());
        }

        return List.of(); // Empty list if user not found
    }
}