package kr.co.talk.global.config.websocket;

import kr.co.talk.domain.chatroom.dto.ChatEnterResponseDto;
import kr.co.talk.domain.chatroom.dto.SocketFlagResponseDto;
import kr.co.talk.domain.chatroomusers.dto.AllRegisteredDto;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Type;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SessionHandler extends StompSessionHandlerAdapter {

    private StompSession session;

    /**
     * exception
     *
     * @param session   the session
     * @param command   the command
     * @param headers   the headers
     * @param payload   the payload
     * @param exception the exception
     */
    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        log.error("exception occured -> Reason : {}", exception.getMessage());
        log.error("stomp headers : [{}], Payload : [{}]", headers, new String(payload));
    }

    /**
     * subscribed url
     */
    public void subscribe(String destination) {
        session.subscribe(destination, this);
        log.debug("[ {} ] subscribed", destination);
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        if (headers.getDestination().equals("/pub/enter")) {
            return ChatEnterResponseDto.class;
        } else if (headers.getDestination().equals("/pub/select/keyword")) {
            return SocketFlagResponseDto.class;
        } else if (headers.getDestination().equals("/pub/question-order")) {
            return AllRegisteredDto.class;
        }
        return Object.class;
    }

}
