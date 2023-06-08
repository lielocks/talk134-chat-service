package kr.co.talk.domain.chatroom.service;

import kr.co.talk.domain.chatroom.dto.EmoticonResponseDto;
import kr.co.talk.domain.chatroom.model.Chatroom;
import kr.co.talk.domain.chatroom.repository.ChatroomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EmoticonService {
    private final ChatroomRepository chatroomRepository;

    public Chatroom getChatroomById(long roomId) {
        return chatroomRepository.findChatroomByChatroomId(roomId);
    }

    public EmoticonResponseDto saveEmoticonHistoryToRedis() {
        return EmoticonResponseDto.builder()
                .build();
    }
}
