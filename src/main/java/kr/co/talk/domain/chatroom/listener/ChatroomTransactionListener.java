package kr.co.talk.domain.chatroom.listener;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import kr.co.talk.domain.chatroom.dto.ChatEnterResponseDto;
import kr.co.talk.domain.chatroom.dto.ChatroomNoticeDto;
import kr.co.talk.domain.chatroom.dto.SocketType;
import kr.co.talk.domain.chatroom.model.event.CreateRoomNotiEventModel;
import kr.co.talk.global.constants.RedisConstants;
import kr.co.talk.global.constants.StompConstants;
import kr.co.talk.global.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatroomTransactionListener {

    private final SimpMessagingTemplate template;
    private final RedisService redisService;
    
    /**
     * chatroom create후 대화방 알림 처리
     * @param eventModel
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notiCreateChatroomListener(CreateRoomNotiEventModel eventModel) {
        log.info("chatroom create transactional after commit listener");
        
        ChatroomNoticeDto chatroomNoticeDto = eventModel.getChatroomNoticeDto();
        
        redisService.pushMap(RedisConstants.ROOM_NOTICE, String.valueOf(chatroomNoticeDto.getRoomId()), chatroomNoticeDto);
        
        eventModel.getUserId().forEach(uid->{
            ChatEnterResponseDto responseDto = ChatEnterResponseDto.builder()
                    .type(SocketType.NEW_CHATROOM)
                    .build();
            template.convertAndSend(StompConstants.getPrivateChannelDestination(uid), responseDto);
        });
    }
}
