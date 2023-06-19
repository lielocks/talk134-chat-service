package kr.co.talk.domain.questionnotice.service;

import kr.co.talk.domain.chatroom.model.Chatroom;
import kr.co.talk.domain.chatroom.repository.ChatroomRepository;
import kr.co.talk.domain.chatroomusers.entity.ChatroomUsers;
import kr.co.talk.domain.chatroomusers.repository.ChatroomUsersRepository;
import kr.co.talk.domain.questionnotice.dto.QuestionNoticeResponseDto;
import kr.co.talk.global.client.UserClient;
import kr.co.talk.global.constants.RedisConstants;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import kr.co.talk.global.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
        List<String> list = getQuestionList(roomId);
        if (CollectionUtils.isEmpty(list)) {
            
        }
        Chatroom chatroom = chatroomRepository.findChatroomByChatroomId(roomId);
        var chatroomUsersList = chatroomUsersRepository.findChatroomUsersByChatroom(chatroom);
        if (chatroomUsersList.isEmpty()) {
            throw new CustomException(CustomError.CHATROOM_DOES_NOT_EXIST);
        }
        var userList = userClient.requiredEnterInfo(0, chatroomUsersList.stream()
                .map(ChatroomUsers::getUserId)
                .collect(Collectors.toUnmodifiableList()));
        var randomUser = userList.get(RandomUtils.nextInt(userList.size()));
        // TODO: redis에서 값 읽어오고 순서처리
        List<Long> questionCodes = redisService.findQuestionCode(roomId, randomUser.getUserId());
        // TODO: topic 세팅
        return QuestionNoticeResponseDto.builder()
                .speaker(randomUser)
                .userList(userList)
                .build();
    }

    private List<String> getQuestionList(long roomId) {
        return redisService.getList(getQuestionKey(roomId));
    }

    private String getQuestionKey(long roomId) {
        return String.format("%s_%s", roomId, RedisConstants.QUESTION_NOTICE);
    }

    private void saveCurrentQuestionStatus() {

    }
}
