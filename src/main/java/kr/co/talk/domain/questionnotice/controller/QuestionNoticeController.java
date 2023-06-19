package kr.co.talk.domain.questionnotice.controller;

import kr.co.talk.domain.questionnotice.dto.QuestionNoticeResponseDto;
import kr.co.talk.domain.questionnotice.service.QuestionNoticeService;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import kr.co.talk.global.exception.ErrorDto;
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
        try {
            QuestionNoticeResponseDto dto = questionNoticeService.getQuestionNotice(roomId);
            template.convertAndSend(generateSubUrl(roomId), dto);
        } catch (CustomException e) {
            sendError(roomId, e.getCustomError());
        }
    }

    private String generateSubUrl(long roomId) {
        return SUB_URL + roomId;
    }

    private void sendError(long roomId, CustomError error) {
        template.convertAndSend(generateSubUrl(roomId), ErrorDto.createErrorDto(error));
    }
}
