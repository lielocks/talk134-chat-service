package kr.co.talk.domain.chatroom.service;

import kr.co.talk.domain.chatroom.dto.*;
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
import java.util.Optional;
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
        setUserInfoRedis(chatEnterDto);
        return ChatEnterResponseDto.builder().type(setSocketType(chatEnterDto.getUserId(), chatEnterDto.getRoomId())).checkInFlag(after10Minutes(chatEnterDto))
                .chatroomUserInfos(getResponseDto(chatEnterDto)).requestId(chatEnterDto.getUserId()).build();
    }

    private List<ChatEnterResponseDto.ChatroomUserInfo> getResponseDto(ChatEnterDto chatEnterDto) {
        boolean flag = chatEnterDto.isSelected();
        Chatroom chatroom = chatroomRepository.findChatroomByChatroomId(chatEnterDto.getRoomId());
        if (chatroom == null) {
            throw new CustomException(CustomError.CHATROOM_DOES_NOT_EXIST);
        }
        List<ChatroomUsers> chatroomUsers = getChatroomUsers(chatroom);
        ChatroomUsers chatroomUsersByUserId = usersRepository.findChatroomUsersByChatroomIdAndUserId(chatEnterDto.getRoomId(), chatEnterDto.getUserId());

        chatroomUsersByUserId.activeFlagOn(flag);
        chatroomUsersByUserId.setEntered(true);

        List<Long> idList = chatroomUsers.stream()
                .map(ChatroomUsers::getUserId)
                .collect(Collectors.toList());

        if (idList == null) {
            throw new CustomException(CustomError.USER_DOES_NOT_EXIST);
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
        List<ChatEnterResponseDto.ChatroomUserInfo> chatUserInfos = new ArrayList<>();
        for (Long userId : idList) {

            RequestDto.ChatRoomEnterResponseDto enterDto = enterResponseDto.stream()
                    .filter(dto -> dto.getUserId().equals(userId))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(CustomError.CHATROOM_DOES_NOT_EXIST));

            ChatroomUsers byChatroomIdAndUserId = usersRepository.findChatroomUsersByChatroomIdAndUserId(chatEnterDto.getRoomId(), userId);

            ChatEnterResponseDto.ChatroomUserInfo responseUserInfo = new ChatEnterResponseDto.ChatroomUserInfo(
                    enterDto.getUserId(),
                    enterDto.getNickname(),
                    enterDto.getUserName(),
                    enterDto.getProfileUrl(),
                    byChatroomIdAndUserId.isActiveFlag(),
                    finalFlag
            );
            chatUserInfos.add(responseUserInfo);
        }

        return chatUserInfos;
    }

    private void setUserInfoRedis(ChatEnterDto chatEnterDto) {
        String key = chatEnterDto.getUserId() + RedisConstants.CHATROOM;
        ChatroomUsers chatroomUsersByUserId = usersRepository.findChatroomUsersByChatroomIdAndUserId(chatEnterDto.getRoomId(), chatEnterDto.getUserId());
        if (chatroomUsersByUserId == null) {
            throw new CustomException(CustomError.CHATROOM_DOES_NOT_EXIST);
        }
        redisService.pushUserChatRoom(chatEnterDto.getUserId(), chatEnterDto.getRoomId());
        redisService.roomCreateTime(chatEnterDto.getRoomId(), chatEnterDto.getUserId());
        String redisValue = redisService.getValues(key);
        log.info("redis Value :: {}", redisValue);
        boolean b = redisValue.equals(String.valueOf(chatEnterDto.getRoomId()));

        if (!b) {
            throw new CustomException(CustomError.CHATROOM_USER_ALREADY_JOINED);
        }
    }

    /**
     *  1. 채팅방이 만들어진 후 10분 지난 시점에서 socketFlag가 1로 바뀌지 않았을때
     *  2. activeFlag가 true인 사람들이 과반수 이상일때
     *  3. activeFlag가 false인 사람들 모두 true로 바꿔줌 -> 자동으로 socketFlag 1로 변경됨
     */
    public int socketFlagStatus(int socketFlag, ChatEnterDto chatEnterDto) {
        int flag = 0;
        Chatroom chatroom = chatroomRepository.findChatroomByChatroomId(chatEnterDto.getRoomId());
        ChatroomUsers chatroomUsersByUserId = usersRepository.findChatroomUsersByChatroomIdAndUserId(chatEnterDto.getRoomId(), chatEnterDto.getUserId());

        // 화면 전환으로 enterDto 가 초기값으로 설정되었을때
        Integer currentSocketFlag = getChatroomUsers(chatroom).stream().map(user -> user.getSocketFlag()).findFirst().get();
        if (socketFlag < currentSocketFlag) {
            if (chatEnterDto.isSelected()) {
                chatroomUsersByUserId.activeFlagOn(false);
            }
            return currentSocketFlag;
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
            redisService.chatRoomCreateTime(chatEnterDto.getRoomId());
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

    @Transactional
    public void disconnectUserSetFalse(long userId, long roomId) {
        ChatroomUsers user = usersRepository.findChatroomUsersByChatroomIdAndUserId(roomId, userId);
        user.activeFlagOn(false);
    }

    public boolean userStatus(long userId, long roomId) {
        ChatroomUsers user = usersRepository.findChatroomUsersByChatroomIdAndUserId(roomId, userId);
        return user.isActiveFlag();
    }

    private boolean allChatroomUsersActive(List<ChatroomUsers> chatroomUsers) {
        return chatroomUsers.stream().allMatch(ChatroomUsers::isActiveFlag);
    }

    private boolean checkTrueStatusGoe(Chatroom chatroom) {
        List<ChatroomUsers> chatroomUsers = getChatroomUsers(chatroom);
        List<ChatroomUsers> flagTrueUsersList = chatroomUsers.stream().filter(ChatroomUsers::isActiveFlag).collect(Collectors.toList());
        return flagTrueUsersList.size() >= Math.ceil(chatroomUsers.size() / 2.0);
    }

    // 대화 참여자가 1/2 이상 참여하기 버튼을 누른 상태 -> 10분 이상이 지나면 set activeFlag true
    private String after10Minutes(ChatEnterDto chatEnterDto) {
        String verify = "";
        Chatroom chatroom = chatroomRepository.findChatroomByChatroomId(chatEnterDto.getRoomId());
        List<ChatroomUsers> currentUsers = usersRepository.findChatroomUsersByChatroom(chatroom);
        Optional<Boolean> socketFlagTwo = currentUsers.stream().map(user -> user.getSocketFlag() == 2).findFirst();

        boolean statusGoe = checkTrueStatusGoe(chatroom);

        if (chatEnterDto.getSocketFlag() == 0 && Boolean.FALSE.equals(socketFlagTwo.get()) && statusGoe && redisService.isWithin10Minutes(currentUsers)) {
            setAllChatroomUsersActiveFlag(currentUsers, true);
            verify = "true";
        } else if (chatEnterDto.getSocketFlag() == 0 && Boolean.FALSE.equals(socketFlagTwo.get()) && !statusGoe && redisService.isWithin10Minutes(currentUsers)) {
            verify = "stillFalse";
        } else if (chatEnterDto.getSocketFlag() == 2 && !redisService.findChatRoomTime(chatEnterDto.getRoomId())) {
            verify = "false";
        } else if (chatEnterDto.getSocketFlag() == 2 && Boolean.TRUE.equals(socketFlagTwo.get()) && statusGoe && redisService.flagSetWithin10Minutes(currentUsers)) {
            setAllChatroomUsersActiveFlag(currentUsers, true);
            verify = "true";
        } else if (chatEnterDto.getSocketFlag() == 2 && Boolean.TRUE.equals(socketFlagTwo.get()) && !statusGoe && redisService.flagSetWithin10Minutes(currentUsers)) {
            verify = "stillFalse";
        } else {
            verify = "false";
        }

        return verify;
    }

    private SocketType setSocketType(long userId, long roomId) {
        ChatroomUsers user = usersRepository.findChatroomUsersByChatroomIdAndUserId(roomId, userId);
        if (Boolean.TRUE.equals(user.isEntered())) {
            return SocketType.RE_ENTER;
        } else return SocketType.NEW_CHATROOM;
    }

}
