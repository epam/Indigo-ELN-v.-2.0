package com.epam.indigoeln.config;


import com.epam.indigoeln.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Arrays;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfiguration extends AbstractWebSocketMessageBrokerConfigurer {

    public static final String IP_ADDRESS = "IP_ADDRESS";

    @Autowired
    private Environment environment;

    @Value("${cors.origin}")
    private String corsOrigin;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic","/queue");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.setInterceptors(new SecurityContextChannelInterceptor());
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        StompWebSocketEndpointRegistration endpoint = registry.addEndpoint("/websocket");

        if (Arrays.asList(environment.getActiveProfiles()).contains(Application.Profile.CORS)) {
            endpoint.setAllowedOrigins(corsOrigin);
        }

        endpoint.withSockJS().setInterceptors(httpSessionHandshakeInterceptor());
    }

    @Bean
    public HandshakeInterceptor httpSessionHandshakeInterceptor() {
        return new HandshakeInterceptorImpl();
    }

    private static class HandshakeInterceptorImpl implements HandshakeInterceptor {
        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
            if (request instanceof ServletServerHttpRequest) {
                ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                attributes.put(IP_ADDRESS, servletRequest.getRemoteAddress());
            }
            return true;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
            // Do nothing
        }
    }
}
