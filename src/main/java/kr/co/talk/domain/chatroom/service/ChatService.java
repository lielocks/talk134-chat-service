package kr.co.talk.domain.chatroom.service;

import kr.co.talk.domain.chatroom.dto.ChatEnterDto;
import kr.co.talk.domain.chatroom.dto.ChatEnterResponseDto;
import kr.co.talk.domain.chatroom.dto.RequestDto;
import kr.co.talk.domain.chatroom.model.Chatroom;
import kr.co.talk.domain.chatroom.repository.ChatroomRepository;
import kr.co.talk.domain.chatroomusers.entity.ChatroomUsers;
import kr.co.talk.domain.chatroomusers.repository.ChatroomUsersRepository;
import kr.co.talk.global.client.UserClient;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {
    private final ChatroomUsersRepository usersRepository;
    private final ChatroomRepository chatroomRepository;
    private final UserClient userClient;

    @Transactional
    public List<ChatEnterResponseDto> sendChatMessage(ChatEnterDto chatEnterDto) {
        boolean flag = chatEnterDto.isSelected() ? true : false;
        Chatroom chatroom = chatroomRepository.findChatroomByChatroomId(chatEnterDto.getRoomId());
        if (chatroom == null) {
            throw new CustomException(CustomError.CHATROOM_DOES_NOT_EXIST);
        }
        List<ChatroomUsers> chatroomUsers = usersRepository.findChatroomUsersByChatroom(chatroom);
        ChatroomUsers chatroomUsersByUserId = usersRepository.findChatroomUsersByChatroomIdAndUserId(chatEnterDto.getRoomId(), chatEnterDto.getUserId());
        chatroomUsersByUserId.activeFlagOn(flag);
        log.info("chatroomUsersByUserId userId chatroomUsersByUserId chatroomUsersByUserId here ?? ?? ?? ?? :: {}", chatroomUsersByUserId);
        log.info(":::::: chatroomUsersByUserId :::: {}", chatroomUsersByUserId.isActiveFlag());

        List<Long> idList = chatroomUsers.stream()
                .map(ChatroomUsers::getUserId)
                .collect(Collectors.toList());
        log.info("idList ::::::::::::::::::::::::: {} ", idList);
        if (idList == null) {
            throw new CustomException(CustomError.CHATROOM_DOES_NOT_EXIST);
        }
        log.info("userIdAndChatroom 111111   ::::::::::::::::::::  {}{}", chatEnterDto.getUserId(), chatroom.getChatroomId());
        log.info("roomId ::: {}", chatroom.getChatroomId());

        List<RequestDto.ChatRoomEnterResponseDto> enterResponseDto = userClient.requiredEnterInfo(chatEnterDto.getUserId(), idList);
        log.info("idList in enterResponseDto :: {}", idList);
        log.info("OOOOOOOOOOOOOOOOOOOOOO LL :: {}", enterResponseDto);

        List<ChatEnterResponseDto> listResponseDto = new ArrayList<>();
        for (Long userId : idList) {
            log.info("is userID here ?? ?? ?? ?? :: {}", userId);
            usersRepository.findChatroomUsersByUserId(userId);

            RequestDto.ChatRoomEnterResponseDto enterDto = enterResponseDto.stream()
                    .filter(dto -> dto.getUserId().equals(userId))
                    .findFirst()
                    .orElse(null);

            ChatroomUsers byChatroomIdAndUserId = usersRepository.findChatroomUsersByChatroomIdAndUserId(chatEnterDto.getRoomId(), userId);

            ChatEnterResponseDto responseDto = new ChatEnterResponseDto(
                    enterDto.getNickname(),
                    enterDto.getUserName(),
                    enterDto.getProfileUrl(),
                    byChatroomIdAndUserId.isActiveFlag()
            );
            listResponseDto.add(responseDto);
        }
        return listResponseDto;

    }

}
