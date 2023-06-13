package kr.co.talk.domain.chatroom.controller;

import kr.co.talk.domain.chatroom.dto.ChatEnterDto;
import kr.co.talk.domain.chatroom.dto.ChatEnterResponseDto;
import kr.co.talk.domain.chatroom.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final SimpMessagingTemplate template;

    @MessageMapping("/enter")
    public void message(@Payload ChatEnterDto chatEnterDto) {
        ChatEnterResponseDto responseDto = chatService.sendChatMessage(chatEnterDto);
        log.info("chatEnterDto RoomId in responseDto :: {} ", chatEnterDto.getRoomId());
        log.info("chatEnterDto UserId in responseDto :: {} ", chatEnterDto.getUserId());
        log.info("Full ResponseDto :: {} ", responseDto);
        template.convertAndSend("/sub/chat/room/" + chatEnterDto.getRoomId(), responseDto);
    }
}
