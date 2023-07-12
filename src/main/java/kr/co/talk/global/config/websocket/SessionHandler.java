package kr.co.talk.global.config.websocket;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SessionHandler extends StompSessionHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SessionHandler.class);

    private StompSession session;

    /**
     * Handle exception.
     *
     * @param session   the session
     * @param command   the command
     * @param headers   the headers
     * @param payload   the payload
     * @param exception the exception
     */
    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        logger.error("Got an exception. Reason : {}", exception.getMessage());
        logger.error("StompHeaders : [{}], Payload : [{}]", headers, new String(payload));
    }

    /**
     * Subscribe.
     *
     * @param destination the destination
     */
    public synchronized void subscribe(String destination) {
        session.subscribe(destination, this);
        logger.debug("[{}] Subscribed.", destination);
    }
}
