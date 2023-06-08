package kr.co.talk.global.config.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.talk.domain.chatroom.dto.ChatEnterDto;
import kr.co.talk.domain.chatroom.dto.ChatEnterResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final HashMap<String, WebSocketSession> sessionMap = new HashMap<>();

    /**
     * key : session ID
     * value : session
     */

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //소켓 연결
        super.afterConnectionEstablished(session);
        sessionMap.put(session.getId(), session);
        log.info("소켓 시작 {}", session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws JsonProcessingException {
        //메시지 발송
        String msg = message.getPayload();
        log.info("message getPayload log info :: {}", msg);

        ChatEnterResponseDto chatEnterResponseDto = objectMapper.readValue(msg, ChatEnterResponseDto.class);
        log.info("session {}", chatEnterResponseDto.toString());

        for (String key : sessionMap.keySet()) {
            WebSocketSession ws = sessionMap.get(key);
            try {
                if (ws.isOpen()) {
                    ws.sendMessage(new TextMessage(msg));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        //소켓 종료
        sessionMap.remove(session.getId());
        super.afterConnectionClosed(session, status);
        log.info("소켓 종료 {}", session.getId());
    }
}

