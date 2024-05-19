package kr.co.talk.global.config.websocket;

import kr.co.talk.domain.chatroom.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class SocketEventListener {
    private final ChatService chatService;

    private static Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    /**
     * Handle session connected events.
     *
     * @param event the event
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        //GenericMessage msg = (GenericMessage) headerAccessor.getMessageHeaders().get("simpConnectMessage");

        log.info("Received a new web socket connection. Session ID : [{}]", headerAccessor.getSessionId());
    }

    /**
     * Handle session disconnected events.
     *
     * @param event the event
     */
    @EventListener
    public void handleWebSocketDisConnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

        String sessionId = findBrowserSessionId(headerAccessor.getSessionId());

        if(sessionId != null) {
            sessionMap.remove(headerAccessor.getSessionId());
        }

        if (sessionAttributes.get("userId") != null && sessionAttributes.get("roomId") != null) {
            chatService.disconnectUserSetFalse((Long) sessionAttributes.get("userId"), (Long) sessionAttributes.get("roomId"));
        }
        log.info("Web socket session closed. Message : [{}]", event.getMessage());
    }

    /**
     * Find session id by session id.
     *
     * @param sessionId
     * @return
     */
    public String findBrowserSessionId(String sessionId) {
        String session = null;

        for (Map.Entry<String, WebSocketSession> entry : sessionMap.entrySet()) {
            if (entry.getKey().equals(sessionId)) {
                session = entry.getKey();
            }
        }

        return session;
    }


    public MessageHeaders createHeaders(SimpMessageHeaderAccessor headerAccessor, long userId, long roomId) {
        headerAccessor.getSessionAttributes().put("userId", userId);
        headerAccessor.getSessionAttributes().put("roomId", roomId);
        headerAccessor.setLeaveMutable(true);

        return headerAccessor.getMessageHeaders();
    }

}
