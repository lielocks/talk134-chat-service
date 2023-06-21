package kr.co.talk.domain.timeoutnotice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TimeoutNoticeService {
    private static final String TIMEOUT_NOTICE_SUB_URL = "/sub/chat/room/timeout/";

    private final SimpMessagingTemplate messagingTemplate;

    public void sendFiveMinuteLeftMessage(long roomId) {
        sendTimeoutMessage(roomId, true);
    }

    public void sendTimedOutMessage(long roomId) {
        sendTimeoutMessage(roomId, false);
    }

    private void sendTimeoutMessage(long roomId, boolean fiveMinute) {
        TimeoutNoticeDto dto = TimeoutNoticeDto.builder()
                .roomId(roomId)
                .fiveMinuteLeft(fiveMinute)
                .build();
        messagingTemplate.convertAndSend(generateTimeoutSubUrl(roomId), dto);
    }

    private String generateTimeoutSubUrl(long roomId) {
        return TIMEOUT_NOTICE_SUB_URL + roomId;
    }
}
