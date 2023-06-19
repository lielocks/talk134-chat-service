package kr.co.talk.domain.questionnotice.controller;

import kr.co.talk.domain.questionnotice.dto.QuestionNoticeResponseDto;
import kr.co.talk.domain.questionnotice.service.QuestionNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class QuestionNoticeController {
    private static final String SUB_URL = "/sub/chat/room/question-notice/";
    private final QuestionNoticeService questionNoticeService;
    private final SimpMessagingTemplate template;

    @MessageMapping("/question-notice/{roomId}")
    public void publishQuestionNotification(@DestinationVariable Long roomId) {
        if (roomId == null) {
            return;
        }
        // TODO: Exception handling
        QuestionNoticeResponseDto dto = questionNoticeService.getQuestionNotice(roomId);
        template.convertAndSend(generateSubUrl(roomId), dto);
    }
    
    private String generateSubUrl(long roomId) {
        return SUB_URL + String.valueOf(roomId);
    }
}
