package kr.co.talk.domain.chatroom.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class QuestionNotifyingController {

    @SendTo("/sub/chat/room/question-notification/{roomId}")
    @MessageMapping("/question-notification/{roomId}")
    public void publishQuestionNotification(@DestinationVariable Long roomId) {
        // 질문 랜덤하게 선택

    }
}
