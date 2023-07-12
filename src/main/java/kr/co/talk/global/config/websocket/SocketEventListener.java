package kr.co.talk.global.config.websocket;

import kr.co.talk.domain.chatroom.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import javax.websocket.Session;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocketEventListener {
    private final ChatService chatService;
    private static final Logger logger = LoggerFactory.getLogger(SocketEventListener.class);

    private static java.util.Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<String, WebSocketSession>();

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

        String sessionId = findBrowserSessionId(headerAccessor.getSessionId());
        if(sessionId != null) {
            sessionMap.remove(headerAccessor.getSessionId());
        }

        Optional<Object> userIdOpt = Optional.ofNullable(headerAccessor.getSessionAttributes().get("userId"));
        Optional<Object> roomIdOpt = Optional.ofNullable(headerAccessor.getSessionAttributes().get("roomId"));
        if (userIdOpt.isPresent() && roomIdOpt.isPresent()) {
            long userId = (Long) userIdOpt.get();
            long roomId = (Long) roomIdOpt.get();
            chatService.disconnectUserSetFalse(userId, roomId);
            log.info("verify the user changed to false :: {}", chatService.userStatus(userId, roomId));
        }

        logger.info("Web socket session closed. Message : [{}]", event.getMessage());
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

    /**
     * Register browser session.
     *
     * @param session the browser session
     * @param sessionId  the session id
     */
    public synchronized void registerBrowserSession(WebSocketSession session, String sessionId) {
        sessionMap.put(sessionId, session);
    }


    /**
     * Create headers message headers.
     *
     * @param sessionId the session id
     * @return the message headers
     */
    public MessageHeaders createHeaders(String sessionId, long userId, long roomId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.getSessionAttributes().put("userId", userId);
        headerAccessor.getSessionAttributes().put("roomId", roomId);
        headerAccessor.setLeaveMutable(true);

        log.info("Header Accessor log info :: {}", headerAccessor.getSessionAttributes());
        return headerAccessor.getMessageHeaders();
    }

}
