package kr.co.talk.domain.chatroom.controller;

import kr.co.talk.domain.chatroom.dto.ChatEnterDto;
import kr.co.talk.domain.chatroom.dto.ChatEnterResponseDto;
import kr.co.talk.domain.chatroom.service.ChatService;
import kr.co.talk.global.constants.StompConstants;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import kr.co.talk.global.exception.ErrorDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final SimpMessagingTemplate template;

    @MessageMapping("/enter")
    public void message(@Payload ChatEnterDto chatEnterDto, SimpMessageHeaderAccessor headerAccessor) {
        try {
            if (!chatService.userEnteredStatus(chatEnterDto.getUserId(), chatEnterDto.getRoomId())) {
                ChatEnterResponseDto responseDto = chatService.sendChatMessage(chatEnterDto);
                log.info("responseDto :: {}", responseDto);
                template.convertAndSend(StompConstants.getRoomEnterDestination(chatEnterDto.getRoomId()), responseDto);
            } else {
                ChatEnterResponseDto oneResponseDto = chatService.getOneResponseDto(chatEnterDto);
                log.info("oneResponseDto :: {}", oneResponseDto);
                template.convertAndSend(StompConstants.getPrivateChannelDestination(chatEnterDto.getUserId()), oneResponseDto);
            }

            headerAccessor.getSessionAttributes().put("userId", chatEnterDto.getUserId());
            headerAccessor.getSessionAttributes().put("roomId", chatEnterDto.getRoomId());
            log.info("current header accessor attributes :: {}", headerAccessor.getSessionAttributes());
        }
        catch (CustomException e) {
            if (e.getCustomError() == CustomError.CHATROOM_DOES_NOT_EXIST) {
                chatroomNotExist(chatEnterDto.getRoomId());
            } else if (e.getCustomError() == CustomError.USER_DOES_NOT_EXIST) {
                userNotExist(chatEnterDto.getRoomId());
            } else if (e.getCustomError() == CustomError.CHATROOM_USER_ALREADY_JOINED) {
                blockSameUser(chatEnterDto.getRoomId());
            }
        }
    }

    @EventListener
    public void webSocketDisconnectListener(SessionDisconnectEvent event) {
        log.info("disconnected event :: {}", event);
        // 어떤 userId, roomId 정보를 가진 session 이 끊겼는지 get message
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        long roomId = (Long) headerAccessor.getSessionAttributes().get("roomId");

        chatService.disconnectUserSetFalse(userId, roomId);
        log.info("verify the user changed to false :: {}", chatService.userStatus(userId, roomId));
    }

    private void blockSameUser(Long roomId) {
        template.convertAndSend(StompConstants.getRoomEnterDestination(roomId), ErrorDto.createErrorDto(CustomError.CHATROOM_USER_ALREADY_JOINED));
        log.info("get the Destination of CHATROOM USER ALREADY JOINED ERROR :: {}", StompConstants.getRoomEnterDestination(roomId));
    }

    private void chatroomNotExist(Long roomId) {
        template.convertAndSend(StompConstants.getRoomEnterDestination(roomId), ErrorDto.createErrorDto(CustomError.CHATROOM_DOES_NOT_EXIST));
        log.info("get the Destination of CHATROOM NOT EXIST ERROR :: {}", StompConstants.getRoomEnterDestination(roomId));
    }

    private void userNotExist(Long roomId) {
        template.convertAndSend(StompConstants.getRoomEnterDestination(roomId), ErrorDto.createErrorDto(CustomError.USER_DOES_NOT_EXIST));
        log.info("get the Destination of USER NOT EXIST ERROR :: {}", StompConstants.getRoomEnterDestination(roomId));
    }

}
