package kr.co.talk.global.config.websocket;

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
        log.error("Got an exception. Reason : {}", exception.getMessage());
        log.error("StompHeaders : [{}], Payload : [{}]", headers, new String(payload));
    }

    /**
     * subscribed url
     */
    public synchronized void subscribe(String destination) {
        session.subscribe(destination, this);
        log.debug("[{}] Subscribed.", destination);
    }

}
