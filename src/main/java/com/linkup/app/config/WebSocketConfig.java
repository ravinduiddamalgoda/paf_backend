package com.linkup.app.config;

import com.linkup.app.security.JwtTokenProvider;
import com.linkup.app.security.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue", "/user");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = extractToken(accessor);
                    System.out.println(token);
                    if (StringUtils.hasText(token)) {
                        try {
                            if (tokenProvider.validateToken(token)) {
                                String username = tokenProvider.getUsernameFromJWT(token);
                                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                                UsernamePasswordAuthenticationToken auth =
                                        new UsernamePasswordAuthenticationToken(
                                                userDetails, null, userDetails.getAuthorities());

                                SecurityContextHolder.getContext().setAuthentication(auth);
                                accessor.setUser(auth);
                                logger.debug("WebSocket CONNECT authenticated for user {}", username);
                            } else {
                                logger.warn("WebSocket CONNECT: JWT did not validate");
                            }
                        } catch (Exception ex) {
                            // IMPORTANT: do not rethrow, or SockJS session will be closed with 1002
                            logger.warn("WebSocket CONNECT token validation failed, proceeding as anonymous user: {}",
                                    ex.getMessage());
                        }
                    } else {
                        logger.debug("WebSocket CONNECT without Authorization header, anonymous session");
                    }
                }
                return message;
            }
        });
    }

    private String extractToken(StompHeaderAccessor accessor) {
        String bearerToken = accessor.getFirstNativeHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            logger.debug("Token Found....");
            return bearerToken.substring(7);
        }
        logger.debug("No Token Found....");
        return null;
    }
}
