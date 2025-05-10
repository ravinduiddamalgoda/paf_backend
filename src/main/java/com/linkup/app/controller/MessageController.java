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

import java.security.Principal;
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
    public void processMessage(
            @Payload MessageRequest messageRequest,
            Principal principal           // <- injected from your interceptor
    ) {
        if (principal == null) {
            logger.error("Unauthenticated WebSocket user tried to send a message.");
            return;
        }
        String email = principal.getName();
        Optional<User> senderOpt = userRepository.findByEmail(email);
        if (senderOpt.isEmpty()) {
            logger.error("Sender not found: {}", email);
            return;
        }

        User sender = senderOpt.get();
        Message saved = messageService.saveMessage(
                sender.getUserId(),
                messageRequest.getReceiverId(),
                messageRequest.getContent()
        );

        MessageResponse resp = new MessageResponse(
                saved.getMessageId(),
                saved.getSender().getUserId(),
                saved.getSender().getUserName(),
                saved.getReceiver().getUserId(),
                saved.getReceiver().getUserName(),
                saved.getContent(),
                saved.getTimestamp()
        );

        // send to recipient’s queues
        String dest = saved.getReceiver().getUserId().toString();
        messagingTemplate.convertAndSendToUser(dest, "/queue/messages", resp);
        messagingTemplate.convertAndSendToUser(
                dest,
                "/queue/notification",
                new ChatNotification(
                        sender.getUserId(),
                        sender.getUserName(),
                        "New message from " + sender.getUserName()
                )
        );
        logger.info("Message sent from {} to {}", sender.getUserId(), messageRequest.getReceiverId());
    }

    @MessageMapping("/message/delivered")
    public void markAsDelivered(@Payload Long messageId, Principal principal) {
        if (principal != null) {
            messageService.markAsDelivered(messageId);
        }
    }

    @MessageMapping("/message/read")
    public void markAsRead(@Payload Long messageId, Principal principal) {
        if (principal != null) {
            messageService.markAsRead(messageId);
        }
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