package kr.co.talk.domain.chatroom.controller;

import kr.co.talk.domain.chatroom.dto.ChatEnterDto;
import kr.co.talk.domain.chatroom.dto.ChatEnterResponseDto;
import kr.co.talk.domain.chatroom.dto.SocketFlagResponseDto;
import kr.co.talk.domain.chatroom.service.ChatService;
import kr.co.talk.domain.chatroomusers.dto.AllRegisteredDto;
import kr.co.talk.domain.chatroomusers.dto.KeywordSendDto;
import kr.co.talk.domain.chatroomusers.dto.QuestionCodeDto;
import kr.co.talk.domain.chatroomusers.service.KeywordService;
import kr.co.talk.global.config.websocket.SocketEventListener;
import kr.co.talk.global.constants.StompConstants;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import kr.co.talk.global.exception.ErrorDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import javax.websocket.Session;
import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final KeywordService keywordService;
    private final SimpMessagingTemplate template;
    private final SocketEventListener listener;

    @MessageMapping("/enter")
    public void message(@Payload ChatEnterDto chatEnterDto, SimpMessageHeaderAccessor headerAccessor) {
        try {
            ChatEnterResponseDto responseDto = chatService.sendChatMessage(chatEnterDto);
            template.convertAndSend(StompConstants.getOnlyRoomEnterDestination(chatEnterDto.getRoomId()), responseDto);
            log.info("response :: {}", responseDto);

            listener.createHeaders(headerAccessor, chatEnterDto.getUserId(), chatEnterDto.getRoomId());
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

    @MessageMapping("/select/keyword")
    public void selectUserKeyword(@Payload KeywordSendDto keywordSendDto, SimpMessageHeaderAccessor headerAccessor) {
        try {
            SocketFlagResponseDto responseDto = keywordService.setQuestionWithFlag(keywordSendDto);
            log.info("responseDto :: {}", responseDto);
            template.convertAndSend(StompConstants.getChatUserSelectKeyword(keywordSendDto.getRoomId(), keywordSendDto.getUserId()), responseDto);

            listener.createHeaders(headerAccessor, keywordSendDto.getUserId(), keywordSendDto.getRoomId());
        }
        catch (CustomException e) {
            if (e.getCustomError() == CustomError.KEYWORD_DOES_NOT_MATCH) {
                keywordNotMatch(keywordSendDto.getRoomId(), keywordSendDto.getUserId());
            } else if (e.getCustomError() == CustomError.QUESTION_ALREADY_REGISTERED) {
                onlyTwoChances(keywordSendDto.getRoomId(), keywordSendDto.getUserId());
            }
        }
    }

    @MessageMapping("/question-order")
    public void selectQuestionOrder(@Payload QuestionCodeDto questionCodeDto, SimpMessageHeaderAccessor headerAccessor) {
        try {
            AllRegisteredDto allRegisteredDto = keywordService.setQuestionOrder(questionCodeDto);
            log.info("responseDto :: {}", allRegisteredDto);
            template.convertAndSend(StompConstants.getRegisterQuestionOrder(questionCodeDto.getRoomId()), allRegisteredDto);

            listener.createHeaders(headerAccessor, questionCodeDto.getUserId(), questionCodeDto.getRoomId());
        }
        catch (CustomException e) {
            if (e.getCustomError() == CustomError.QUESTION_LIST_SIZE_MISMATCH) {
                questionSizeMismatch(questionCodeDto.getRoomId());
            } else if (e.getCustomError() == CustomError.QUESTION_ORDER_CHANCE_ONCE) {
                orderChanceOnce(questionCodeDto.getRoomId());
            } else if (e.getCustomError() == CustomError.QUESTION_ID_NOT_MATCHED) {
                questionAllContain(questionCodeDto.getRoomId());
            } else if (e.getCustomError() == CustomError.NOT_DISTINCT_QUESTION_LIST) {
                questionCodeDuplicate(questionCodeDto.getRoomId());
            }
        }
    }


    @Scheduled(fixedRate = 10000)
    public void sendHeartbeat() {
        template.convertAndSend("/sub/heartbeat", "HEARTBEAT");
    }

    private void blockSameUser(Long roomId) {
        template.convertAndSend(StompConstants.getOnlyRoomEnterDestination(roomId), ErrorDto.createErrorDto(CustomError.CHATROOM_USER_ALREADY_JOINED));
        log.info("get the Destination of CHATROOM USER ALREADY JOINED ERROR :: {}", StompConstants.getOnlyRoomEnterDestination(roomId));
    }

    private void chatroomNotExist(Long roomId) {
        template.convertAndSend(StompConstants.getOnlyRoomEnterDestination(roomId), ErrorDto.createErrorDto(CustomError.CHATROOM_DOES_NOT_EXIST));
        log.info("get the Destination of CHATROOM NOT EXIST ERROR :: {}", StompConstants.getOnlyRoomEnterDestination(roomId));
    }

    private void userNotExist(Long roomId) {
        template.convertAndSend(StompConstants.getOnlyRoomEnterDestination(roomId), ErrorDto.createErrorDto(CustomError.USER_DOES_NOT_EXIST));
        log.info("get the Destination of USER NOT EXIST ERROR :: {}", StompConstants.getOnlyRoomEnterDestination(roomId));
    }

    private void keywordNotMatch(Long roomId, Long userId) {
        template.convertAndSend(StompConstants.getChatUserSelectKeyword(roomId, userId), ErrorDto.createErrorDto(CustomError.KEYWORD_DOES_NOT_MATCH));
        log.info("get the Destination of KEYWORD DOES NOT MATCH ERROR :: {}", StompConstants.getChatUserSelectKeyword(roomId, userId));
    }

    private void onlyTwoChances(Long roomId, Long userId) {
        template.convertAndSend(StompConstants.getChatUserSelectKeyword(roomId, userId), ErrorDto.createErrorDto(CustomError.QUESTION_ALREADY_REGISTERED));
        log.info("get the Destination of QUESTION ALREADY REGISTERED TWICE ERROR :: {}", StompConstants.getChatUserSelectKeyword(roomId, userId));
    }

    private void questionSizeMismatch(Long roomId) {
        template.convertAndSend(StompConstants.getRegisterQuestionOrder(roomId), ErrorDto.createErrorDto(CustomError.QUESTION_LIST_SIZE_MISMATCH));
        log.info("get the Destination of QUESTION SIZE MISMATCH ERROR :: {}", StompConstants.getRegisterQuestionOrder(roomId));
    }

    private void orderChanceOnce (Long roomId) {
        template.convertAndSend(StompConstants.getRegisterQuestionOrder(roomId), ErrorDto.createErrorDto(CustomError.QUESTION_ORDER_CHANCE_ONCE));
        log.info("get the Destination of QUESTION ORDER CHANCE ONCE ERROR :: {}", StompConstants.getRegisterQuestionOrder(roomId));
    }

    private void questionAllContain (Long roomId) {
        template.convertAndSend(StompConstants.getRegisterQuestionOrder(roomId), ErrorDto.createErrorDto(CustomError.QUESTION_ID_NOT_MATCHED));
        log.info("get the Destination of QUESTION LIST DO NOT CONTAIN ALL ERROR :: {}", StompConstants.getRegisterQuestionOrder(roomId));
    }

    private void questionCodeDuplicate (Long roomId) {
        template.convertAndSend(StompConstants.getRegisterQuestionOrder(roomId), ErrorDto.createErrorDto(CustomError.NOT_DISTINCT_QUESTION_LIST));
        log.info("get the Destination of QUESTION LIST DUPLICATE ERROR :: {}", StompConstants.getRegisterQuestionOrder(roomId));
    }

}
