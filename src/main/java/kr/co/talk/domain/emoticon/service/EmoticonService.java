package kr.co.talk.domain.emoticon.service;

import kr.co.talk.domain.chatroom.dto.RoomEmoticon;
import kr.co.talk.domain.chatroom.model.Chatroom;
import kr.co.talk.domain.chatroom.model.EmoticonCode;
import kr.co.talk.domain.chatroom.repository.ChatroomRepository;
import kr.co.talk.domain.emoticon.dto.EmoticonResponseDto;
import kr.co.talk.domain.emoticon.dto.PubEmoticonPayload;
import kr.co.talk.domain.emoticon.dto.UserReceivedEmoticonDto;
import kr.co.talk.global.constants.RedisConstants;
import kr.co.talk.global.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public UserReceivedEmoticonDto getUserReceivedEmoticons(long roomId, long userId) {
        List<RoomEmoticon> emoticonList = redisService.getEmoticonList(roomId);
        List<UserReceivedEmoticonDto.UserReceivedEmoticon> emoticons = new ArrayList<>();

        emoticonList.stream()
                .filter(emo -> emo.getToUserId() == userId)
                .collect(Collectors.groupingBy(RoomEmoticon::getEmoticonCode))
                .forEach((emoticonCode, roomEmoticons) -> emoticons.add(UserReceivedEmoticonDto.UserReceivedEmoticon.builder()
                        .code(emoticonCode.getCode())
                        .amount(roomEmoticons.size())
                        .build()));

        return UserReceivedEmoticonDto.builder()
                .emoticonList(emoticons)
                .build();
    }

    private String getRoomEmoticonRedisKey(Long roomId) {
        return roomId + RedisConstants.ROOM_EMOTICON;
    }
}
