package kr.co.talk.domain.chatroom.controller;

import kr.co.talk.domain.chatroom.dto.ChatEnterDto;
import kr.co.talk.domain.chatroom.dto.ChatEnterResponseDto;
import kr.co.talk.domain.chatroom.service.ChatService;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import kr.co.talk.global.exception.ErrorDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChatController {
    private static final String CHAT_ROOM_ENTER_DESTINATION = "/sub/chat/room";
    private final ChatService chatService;
    private final SimpMessagingTemplate template;

    @MessageMapping("/enter")
    public void message(@Payload ChatEnterDto chatEnterDto) {
        try {
            ChatEnterResponseDto responseDto = chatService.sendChatMessage(chatEnterDto);
            log.info("Full ResponseDto :: {} ", responseDto);
            template.convertAndSend(getRoomDestination(chatEnterDto.getRoomId()), responseDto);
        } catch (CustomException e) {
            blockSameUser(chatEnterDto.getRoomId());
        }
    }

    private String getRoomDestination(Long roomId) {
        return String.format("%s/%s", CHAT_ROOM_ENTER_DESTINATION, roomId);
    }

    private void blockSameUser(Long roomId) {
        template.convertAndSend(getRoomDestination(roomId), ErrorDto.createErrorDto(CustomError.CHATROOM_USER_ALREADY_JOINED));
        log.info("get the Destination of CHATROOM USER ALREADY ERROR :: {}", getRoomDestination(roomId));
    }
}
