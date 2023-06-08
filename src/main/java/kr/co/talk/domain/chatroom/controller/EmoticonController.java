package kr.co.talk.domain.chatroom.controller;

import kr.co.talk.domain.chatroom.service.EmoticonService;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.ErrorDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class EmoticonController {
    private final EmoticonService emoticonService;
    private final SimpMessagingTemplate template;

    @MessageMapping("/room/emoticon/{roomId}")
    public void publishEmoticon(@DestinationVariable Long roomId) {
        if (roomId == null) {
            sendRoomNotFoundError();
            return;
        }
        if (emoticonService.getChatroomById(roomId) == null) {
            sendRoomNotFoundError();
            return;
        }

        template.convertAndSend("/sub/chat/room", emoticonService.saveEmoticonHistoryToRedis());
    }

    private void sendRoomNotFoundError() {
        template.convertAndSend("/sub/chat/room", ErrorDto.createErrorDto(CustomError.CHATROOM_DOES_NOT_EXIST));
    }
}
