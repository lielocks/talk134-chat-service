package kr.co.talk.domain.questionnotice.controller;

import kr.co.talk.domain.questionnotice.dto.QuestionNoticeResponseDto;
import kr.co.talk.domain.questionnotice.service.QuestionNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class QuestionNoticeController {
    private final QuestionNoticeService questionNoticeService;

    @SendTo("/sub/chat/room/question-notice/{roomId}")
    @MessageMapping("/question-notice/{roomId}")
    public QuestionNoticeResponseDto publishQuestionNotification(@DestinationVariable Long roomId) {
        if (roomId == null) {

        }
        QuestionNoticeResponseDto dto = questionNoticeService.getQuestionNotice(roomId);
        return dto;
    }
}
