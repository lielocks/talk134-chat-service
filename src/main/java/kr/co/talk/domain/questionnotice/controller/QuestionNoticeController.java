package kr.co.talk.domain.questionnotice.controller;

import kr.co.talk.domain.questionnotice.dto.QuestionNoticeResponseDto;
import kr.co.talk.domain.questionnotice.service.QuestionNoticeService;
import kr.co.talk.global.constants.StompConstants;
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
    private final QuestionNoticeService questionNoticeService;
    private final SimpMessagingTemplate template;

    @MessageMapping("/question-notice/{roomId}")
    public void publishQuestionNotification(@DestinationVariable Long roomId) {
        if (roomId == null) {
            return;
        }
        try {
            QuestionNoticeResponseDto dto = questionNoticeService.getQuestionNotice(roomId);
            template.convertAndSend(StompConstants.generateSubUrl(roomId), dto);
        } catch (CustomException e) {
            sendError(roomId, e.getCustomError());
        }
    }

   
    private void sendError(long roomId, CustomError error) {
        template.convertAndSend(StompConstants.generateSubUrl(roomId), ErrorDto.createErrorDto(error));
    }
}
