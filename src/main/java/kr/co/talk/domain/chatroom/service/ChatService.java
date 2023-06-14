package kr.co.talk.domain.chatroom.service;

import kr.co.talk.domain.chatroom.dto.ChatEnterDto;
import kr.co.talk.domain.chatroom.dto.ChatEnterResponseDto;
import kr.co.talk.domain.chatroom.dto.RequestDto;
import kr.co.talk.domain.chatroom.model.Chatroom;
import kr.co.talk.domain.chatroom.repository.ChatroomRepository;
import kr.co.talk.domain.chatroomusers.entity.ChatroomUsers;
import kr.co.talk.domain.chatroomusers.repository.ChatroomUsersRepository;
import kr.co.talk.global.client.UserClient;
import kr.co.talk.global.constants.RedisConstants;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import kr.co.talk.global.service.redis.RedisService;
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
    private final RedisService redisService;

    @Transactional
    public ChatEnterResponseDto sendChatMessage(ChatEnterDto chatEnterDto) throws CustomException {
        String key = chatEnterDto.getUserId() + RedisConstants.CHATROOM;
        redisService.pushUserChatRoom(String.valueOf(chatEnterDto.getUserId()), String.valueOf(chatEnterDto.getRoomId()));
        String redisValue = redisService.getValues(key);
        log.info("redis Value :: {}", redisValue);

        if (!redisValue.equals(String.valueOf(chatEnterDto.getRoomId()))) {
            throw new CustomException(CustomError.CHATROOM_USER_ALREADY_JOINED);
        }

        boolean flag = chatEnterDto.isSelected();
        Chatroom chatroom = chatroomRepository.findChatroomByChatroomId(chatEnterDto.getRoomId());
        if (chatroom == null) {
            throw new CustomException(CustomError.CHATROOM_DOES_NOT_EXIST);
        }
        List<ChatroomUsers> chatroomUsers = getChatroomUsers(chatroom);
        ChatroomUsers chatroomUsersByUserId = usersRepository.findChatroomUsersByChatroomIdAndUserId(chatEnterDto.getRoomId(), chatEnterDto.getUserId());
        chatroomUsersByUserId.activeFlagOn(flag);

        List<Long> idList = chatroomUsers.stream()
                .map(ChatroomUsers::getUserId)
                .collect(Collectors.toList());
        log.info("idList ::::::::::::::::::::::::: {} ", idList);
        if (idList == null) {
            throw new CustomException(CustomError.CHATROOM_DOES_NOT_EXIST);
        }

        List<RequestDto.ChatRoomEnterResponseDto> enterResponseDto = userClient.requiredEnterInfo(chatEnterDto.getUserId(), idList);
        List<ChatroomUsers> usersByUserId = usersRepository.findChatroomUsersByUserId(chatEnterDto.getUserId());

        List<Chatroom> chatrooms = new ArrayList<>();
        List<Long> roomIdList = new ArrayList<>();

        usersByUserId.forEach(ChatroomUsers -> {
            Chatroom usersChatroom = ChatroomUsers.getChatroom();
            chatrooms.add(usersChatroom);
            roomIdList.add(usersChatroom.getChatroomId());
        });

        int finalFlag = socketFlagStatus(chatEnterDto.getSocketFlag(), chatEnterDto);
        List<ChatEnterResponseDto.ChatUserInfo> chatUserInfos = new ArrayList<>();
        for (Long userId : idList) {

            RequestDto.ChatRoomEnterResponseDto enterDto = enterResponseDto.stream()
                    .filter(dto -> dto.getUserId().equals(userId))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(CustomError.CHATROOM_DOES_NOT_EXIST));

            ChatroomUsers byChatroomIdAndUserId = usersRepository.findChatroomUsersByChatroomIdAndUserId(chatEnterDto.getRoomId(), userId);

            ChatEnterResponseDto.ChatUserInfo responseUserInfo = new ChatEnterResponseDto.ChatUserInfo(
                    enterDto.getUserId(),
                    enterDto.getNickname(),
                    enterDto.getUserName(),
                    enterDto.getProfileUrl(),
                    byChatroomIdAndUserId.isActiveFlag(),
                    finalFlag
            );
            chatUserInfos.add(responseUserInfo);
        }

        return ChatEnterResponseDto.builder().chatUserInfoList(chatUserInfos).roomId(roomIdList).build();
    }

    public int socketFlagStatus(int socketFlag, ChatEnterDto chatEnterDto) {
        int flag = 0;
        Chatroom chatroom = chatroomRepository.findChatroomByChatroomId(chatEnterDto.getRoomId());
        if (chatroom == null) {
            throw new CustomException(CustomError.CHATROOM_DOES_NOT_EXIST);
        }

        if (socketFlag == 0) {
            List<ChatroomUsers> chatroomUsers = getChatroomUsers(chatroom);
            boolean allUsersActive = allChatroomUsersActive(chatroomUsers);
            if (allUsersActive) {
                setAllChatroomUsersActiveFlag(chatroomUsers, false);
                flag = 1;
            } else {
                flag = 0;
            }
        }

        if (socketFlag == 2) {
            List<ChatroomUsers> chatroomUsers = getChatroomUsers(chatroom);
            boolean allUsersActive = allChatroomUsersActive(chatroomUsers);
            if (allUsersActive) {
                flag = 3;
            } else {
                flag = 2;
            }
        }

        for (ChatroomUsers user : getChatroomUsers(chatroom)) {
            user.setSocketFlag(flag);
        }
        return flag;
    }

    private List<ChatroomUsers> getChatroomUsers(Chatroom chatroom) {
        List<ChatroomUsers> chatroomUsers = usersRepository.findChatroomUsersByChatroom(chatroom);
        return chatroomUsers;
    }

    private void setAllChatroomUsersActiveFlag(List<ChatroomUsers> chatroomUsers, boolean activeFlag) {
        for (ChatroomUsers user : chatroomUsers) {
            user.activeFlagOn(activeFlag);
        }
    }

    private boolean allChatroomUsersActive(List<ChatroomUsers> chatroomUsers) {
        return chatroomUsers.stream().allMatch(ChatroomUsers::isActiveFlag);
    }

}
