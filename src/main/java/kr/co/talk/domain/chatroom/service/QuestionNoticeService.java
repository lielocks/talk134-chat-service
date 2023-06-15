package kr.co.talk.domain.chatroom.service;

import kr.co.talk.domain.chatroom.dto.QuestionNoticeResponseDto;
import kr.co.talk.domain.chatroom.model.Chatroom;
import kr.co.talk.domain.chatroom.repository.ChatroomRepository;
import kr.co.talk.domain.chatroomusers.entity.ChatroomUsers;
import kr.co.talk.domain.chatroomusers.repository.ChatroomUsersRepository;
import kr.co.talk.global.client.UserClient;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import kr.co.talk.global.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class QuestionNoticeService {
    private final ChatroomRepository chatroomRepository;
    private final ChatroomUsersRepository chatroomUsersRepository;
    private final RedisService redisService;
    private final UserClient userClient;

    @Transactional(readOnly = true)
    public QuestionNoticeResponseDto getQuestionNotice(long roomId) {
        Chatroom chatroom = chatroomRepository.findChatroomByChatroomId(roomId);
        var userList = chatroomUsersRepository.findChatroomUsersByChatroom(chatroom);
        if (userList.isEmpty()) {
            throw new CustomException(CustomError.CHATROOM_DOES_NOT_EXIST);
        }
        var userInfoList = userClient.requiredEnterInfo(0, userList.stream()
                .map(ChatroomUsers::getUserId)
                .collect(Collectors.toUnmodifiableList()));
        var randomUser = userInfoList.get(RandomUtils.nextInt(userInfoList.size()));
        // TODO: redis에서 값 읽어오고 순서처리
//        List<Long> questionCodes = redisService.findQuestionCode(roomId, randomUser.getUserId());
        // TODO: topic 세팅
        return QuestionNoticeResponseDto.builder()
                .speaker(randomUser)
                .userList(userInfoList)
                .build();
    }
}
