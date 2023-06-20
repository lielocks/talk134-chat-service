package kr.co.talk.domain.emoticon.service;

import kr.co.talk.domain.chatroom.dto.RoomEmoticon;
import kr.co.talk.domain.chatroom.model.Chatroom;
import kr.co.talk.domain.chatroom.model.EmoticonCode;
import kr.co.talk.domain.chatroom.repository.ChatroomRepository;
import kr.co.talk.domain.emoticon.dto.EmoticonResponseDto;
import kr.co.talk.domain.emoticon.dto.PubEmoticonPayload;
import kr.co.talk.global.constants.RedisConstants;
import kr.co.talk.global.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EmoticonService {
    private final ChatroomRepository chatroomRepository;
    private final RedisService redisService;

    public Chatroom getChatroomById(long roomId) {
        return chatroomRepository.findChatroomByChatroomId(roomId);
    }

    public EmoticonResponseDto saveEmoticonHistoryToRedis(PubEmoticonPayload payload) throws Exception {
        EmoticonCode emoticonCode = EmoticonCode.of(payload.getEmoticonCode());
        RoomEmoticon value = RoomEmoticon.builder()
                .emoticonCode(emoticonCode)
                .fromUserId(payload.getUserId())
                .toUserId(payload.getToUserId())
                .roomId(payload.getRoomId())
                .build();

        redisService.pushList(getRoomEmoticonRedisKey(payload.getRoomId()), value);

        return EmoticonResponseDto.builder()
                .emoticonCode(payload.getEmoticonCode())
                .build();
    }

    private String getRoomEmoticonRedisKey(Long roomId) {
        return String.format("%s%s", roomId, RedisConstants.ROOM_EMOTICON);
    }
}
