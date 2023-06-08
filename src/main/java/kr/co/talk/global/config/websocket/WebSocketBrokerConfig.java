package kr.co.talk.global.config.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * 정보를 처리할 Handler와 webSocket 주소를 WebSocketHandlerRegistry에 추가해주면
 * 해당 주소로 접근하면 웹소켓 연결이 가능합니다.
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSocket
@EnableWebSocketMessageBroker
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer, WebSocketConfigurer {
    private final WebSocketHandler webSocketHandler;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        registry.addHandler(webSocketHandler, "/ws").setAllowedOriginPatterns("*");
    }
//
//    @Bean
//    @Primary
//    public SimpMessagingTemplate messagingTemplate(MessageChannel clientOutboundChannel) {
//        SimpMessagingTemplate messagingTemplate = new SimpMessagingTemplate(clientOutboundChannel);
//        messagingTemplate.setDefaultDestination("/sub/chat/room"); // Set your default destination here
//        return messagingTemplate;
//    }
}
