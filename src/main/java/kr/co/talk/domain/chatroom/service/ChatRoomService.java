package kr.co.talk.domain.chatroom.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kr.co.talk.domain.chatroom.dto.ChatroomListDto;
import kr.co.talk.domain.chatroom.dto.ChatroomNoticeDto;
import kr.co.talk.domain.chatroom.dto.FeedbackDto;
import kr.co.talk.domain.chatroom.dto.RequestDto;
import kr.co.talk.domain.chatroom.dto.RoomEmoticon;
import kr.co.talk.domain.chatroom.dto.RequestDto.CreateChatroomResponseDto;
import kr.co.talk.domain.chatroom.dto.RequestDto.UserIdResponseDto;
import kr.co.talk.domain.chatroom.dto.RequestDto.UserStatusDto;
import kr.co.talk.domain.chatroom.model.Chatroom;
import kr.co.talk.domain.chatroom.model.EmoticonCode;
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

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatroomRepository chatroomRepository;
    private final ChatroomUsersRepository chatroomUsersRepository;
    private final RedisService redisService;
    private final UserClient userClient;

    /**
     * 닉네임 또는 이름으로 채팅방 목록 조회
     * 
     * @param userId
     * @param teamCode
     * @param name
     * @return
     */
    public List<ChatroomListDto> findChatRoomsByName(long userId, String teamCode,
            List<UserIdResponseDto> userIdResponseDtos) {
        List<Long> findUserIds = userIdResponseDtos.stream().map(dto -> dto.getUserId())
                .collect(Collectors.toList());
        List<Chatroom> chatroomEntity =
                chatroomRepository.findByTeamCodeAndName(teamCode, findUserIds);
        return convertChatRoomListDto(userId, chatroomEntity);
    }

    /**
     * 전체 채팅방 목록 조회
     * 
     * @param userId
     * @param teamCode
     * @return
     */
    public List<ChatroomListDto> findChatRooms(long userId, String teamCode) {
        List<Chatroom> chatroomEntity = chatroomRepository.findByTeamCode(teamCode);
        return convertChatRoomListDto(userId, chatroomEntity);
    }

    public List<ChatroomListDto> convertChatRoomListDto(long userId,
            List<Chatroom> chatroomEntity) {
        return chatroomEntity.stream().map(chatroom -> {
            // emoticon redis에서 조회해서 저장
            List<RoomEmoticon> emoticonList =
                    redisService.getEmoticonList(chatroom.getChatroomId());

            // EmoticonCode별로 grouping
            Map<EmoticonCode, Integer> sizeByCode = emoticonList.stream()
                    .collect(Collectors.groupingBy(
                            RoomEmoticon::getEmoticonCode,
                            Collectors.collectingAndThen(Collectors.toList(), List::size)));

            // 이모티콘 갯수 top3
            List<ChatroomListDto.Emoticons> emoticons = sizeByCode.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(3)
                    .map(entry -> {
                        return ChatroomListDto.Emoticons.builder()
                                .emoticonCode(entry.getKey())
                                .emoticonCount(entry.getValue())
                                .build();
                    }).collect(Collectors.toList());

            List<ChatroomUsers> chatroomUsers = chatroom.getChatroomUsers();
            Optional<ChatroomUsers> optJoinUser =
                    chatroomUsers.stream().filter(u -> u.getUserId() == userId).findAny();

            return ChatroomListDto.builder()
                    .roomId(chatroom.getChatroomId())
                    .roomName(chatroom.getName())
                    .emoticons(emoticons)
                    .chatroomUsers(chatroomUsers)
                    .userCount(chatroomUsers.size())
                    .joinFlag(optJoinUser.isPresent())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public void createChatroom(long createUserId,
            List<Long> userList) {
        if (userList.size() <= 1) {
            throw new CustomException(CustomError.USER_NUMBER_ERROR);
        }

        // 같은 team 인지 validation
        String teamCode = userClient.getTeamCode(createUserId).getTeamCode();
        userList.forEach(uId -> {
            if (!teamCode.equals(userClient.getTeamCode(uId).getTeamCode())) {
                throw new CustomException(CustomError.TEAM_CODE_ERROR);
            }
        });

        CreateChatroomResponseDto requiredCreateChatroomInfo =
                userClient.requiredCreateChatroomInfo(createUserId, userList);

        Chatroom chatroom = Chatroom.builder()
                .name(requiredCreateChatroomInfo.getChatroomName())
                .teamCode(requiredCreateChatroomInfo.getTeamCode())
                .build();

        List<ChatroomUsers> chatroomUsers = userList.stream().map(userId -> {
            return ChatroomUsers.builder()
                    .chatroom(chatroom)
                    .userId(userId)
                    .build();
        }).collect(Collectors.toList());

        chatroomUsersRepository.saveAll(chatroomUsers);

        ChatroomNoticeDto chatroomNoticeDto = ChatroomNoticeDto.builder()
                .roomId(chatroom.getChatroomId())
                .timeout(requiredCreateChatroomInfo.getTimeout())
                .createTime(System.currentTimeMillis())
                .build();

        log.info("chatroom.getChatroomId :: {} ", chatroom.getChatroomId());

        redisService.pushMap(RedisConstants.ROOM_NOTICE, String.valueOf(chatroom.getChatroomId()),
                chatroomNoticeDto);
    }

    /**
     * optional feedback save
     * 
     * @param feedbackDto
     */
    public void saveFeedbackOptionalToRedis(FeedbackDto feedbackDto) {
        // redisService.pushMap(RedisConstants.FEEDBACK_OPTIONAL, null, feedbackDto);
        redisService.pushMap(RedisConstants.FEEDBACK_ + feedbackDto.getRoomId(),
                String.valueOf(feedbackDto.getUserId()), feedbackDto);
    }

    /**
     * 필수 feedback save
     * 
     * @param feedbackDto
     */
    public void saveFeedbackToRedis(long userId, FeedbackDto feedbackDto) {
        RequestDto.UserStatusDto userStausDto = userClient.getUserStaus(userId);

        Map<String, Object> entry = redisService
                .getEntry(RedisConstants.FEEDBACK_ + feedbackDto.getRoomId(), FeedbackDto.class);
        FeedbackDto feedback = (FeedbackDto) entry.get(String.valueOf(userId));

        feedback.setStatusEnergy(feedbackDto.getStatusEnergy());
        feedback.setStatusRelation(feedbackDto.getStatusRelation());
        feedback.setStatusStress(feedbackDto.getStatusStress());
        feedback.setStatusStable(feedbackDto.getStatusStable());

        // before setting
        if (userStausDto.isToday()) {
            feedback.setStatusEnergyBefore(userStausDto.getStatusEnergy());
            feedback.setStatusRelationBefore(userStausDto.getStatusRelation());
            feedback.setStatusStressBefore(userStausDto.getStatusStress());
            feedback.setStatusStableBefore(userStausDto.getStatusStable());
        }

        redisService.pushMap(RedisConstants.FEEDBACK_ + feedback.getRoomId(),
                String.valueOf(feedback.getUserId()), feedback);
    }
}
