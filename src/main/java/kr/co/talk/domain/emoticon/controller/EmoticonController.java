package kr.co.talk.domain.emoticon.controller;

import kr.co.talk.domain.emoticon.dto.PubEmoticonPayload;
import kr.co.talk.domain.emoticon.service.EmoticonService;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.ErrorDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class EmoticonController {
    private static final String CHAT_ROOM_EMOTICON_DESTINATION = "/sub/chat/room/emoticon";

    private final EmoticonService emoticonService;
    private final SimpMessagingTemplate template;

    @MessageMapping("/room/emoticon")
    public void publishEmoticon(@Payload PubEmoticonPayload payload) {
        Long roomId = payload.getRoomId();
        if (roomId == null) {
            return;
        }
        if (emoticonService.getChatroomById(roomId) == null) {
            sendRoomNotFoundError(roomId);
            return;
        }
        try {
            template.convertAndSend(getRoomDestination(roomId), emoticonService.saveEmoticonHistoryToRedis(payload));
            template.convertAndSend(
                    getRoomUserDestination(roomId, payload.getToUserId()),
                    emoticonService.getUserReceivedEmoticons(roomId, payload.getToUserId()));
        } catch (IllegalArgumentException e) {
            sendIllegalArgumentError(roomId);
        } catch (Exception e) {
            sendInternalError(roomId);
        }
    }

    private String getRoomDestination(long roomId) {
        return String.format("%s/%s", CHAT_ROOM_EMOTICON_DESTINATION, roomId);
    }

    private String getRoomUserDestination(long roomId, long userId) {
        return String.format("%s/%s", getRoomDestination(roomId), userId);
    }

    private void sendRoomNotFoundError(Long roomId) {
        template.convertAndSend(getRoomDestination(roomId), ErrorDto.createErrorDto(CustomError.CHATROOM_DOES_NOT_EXIST));
    }

    private void sendIllegalArgumentError(Long roomId) {
        template.convertAndSend(getRoomDestination(roomId), ErrorDto.createErrorDto(CustomError.USER_DOES_NOT_EXIST));
    }

    private void sendInternalError(Long roomId) {
        template.convertAndSend(getRoomDestination(roomId), ErrorDto.createErrorDto(CustomError.SERVER_ERROR));
    }
}
