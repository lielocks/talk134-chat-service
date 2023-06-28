package kr.co.talk.domain.timeoutnotice;

import kr.co.talk.global.constants.StompConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TimeoutNoticeService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendFiveMinuteLeftMessage(long roomId) {
        sendTimeoutMessage(roomId, true);
    }

    public void sendTimedOutMessage(long roomId) {
        sendTimeoutMessage(roomId, false);
    }

    private void sendTimeoutMessage(long roomId, boolean fiveMinute) {
        TimeoutNoticeDto dto = TimeoutNoticeDto.builder()
                .fiveMinuteLeft(fiveMinute)
                .build();
        messagingTemplate.convertAndSend(StompConstants.generateTimeoutSubUrl(roomId), dto);
    }
}
