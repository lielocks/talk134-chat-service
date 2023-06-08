package kr.co.talk.domain.chatroom.service;

import kr.co.talk.domain.chatroom.dto.ChatEnterDto;
import kr.co.talk.domain.chatroom.dto.ChatEnterResponseDto;
import kr.co.talk.domain.chatroom.dto.RequestDto;
import kr.co.talk.domain.chatroom.model.Chatroom;
import kr.co.talk.domain.chatroom.repository.ChatroomRepository;
import kr.co.talk.domain.chatroomusers.entity.ChatroomUsers;
import kr.co.talk.domain.chatroomusers.repository.ChatroomUsersRepository;
import kr.co.talk.global.client.UserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {
    private final ChatroomUsersRepository usersRepository;
    private final ChatroomRepository chatroomRepository;
    private final UserClient userClient;

    @Transactional
    public ChatEnterResponseDto sendChatMessage(ChatEnterDto chatEnterDto) {
        boolean flag;
        RequestDto.ChatRoomEnterResponseDto requiredEnterResponse = userClient.requiredEnterInfo(chatEnterDto.getUserId());
        Chatroom chatroom = chatroomRepository.findChatroomByChatroomId(chatEnterDto.getRoomId());
        if (chatroom == null) {
            throw new IllegalStateException("CHATROOM_DOES_NOT_EXIST");
        }
        ChatroomUsers chatroomUser = ChatroomUsers.builder()
                .chatroom(chatroom)
                .userId(chatEnterDto.getUserId())
                .build();
        log.info("roomId ::: {}", chatroom.getChatroomId());
        if (chatEnterDto.isSelected()) {
            usersRepository.save(chatroomUser);
            flag = true;
        } else {
            flag = false;
        }

        return ChatEnterResponseDto.builder()
                .userName(requiredEnterResponse.getUserName())
                .nickname(requiredEnterResponse.getNickname())
                .activeFlag(flag).build();
    }
}
