package kr.co.talk.domain.questionnotice.service;

import kr.co.talk.domain.chatroom.model.Chatroom;
import kr.co.talk.domain.chatroom.repository.ChatroomRepository;
import kr.co.talk.domain.chatroomusers.entity.ChatroomUsers;
import kr.co.talk.domain.chatroomusers.entity.Question;
import kr.co.talk.domain.chatroomusers.repository.ChatroomUsersRepository;
import kr.co.talk.domain.chatroomusers.repository.QuestionRepository;
import kr.co.talk.domain.chatroomusers.service.KeywordService;
import kr.co.talk.domain.emoticon.dto.EmoticonResponseDto;
import kr.co.talk.domain.questionnotice.dto.QuestionNoticeManagementRedisDto;
import kr.co.talk.domain.questionnotice.dto.QuestionNoticeResponseDto;
import kr.co.talk.domain.questionnotice.dto.QuestionNoticeResponseDto.Topic;
import kr.co.talk.global.client.UserClient;
import kr.co.talk.global.constants.StompConstants;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import kr.co.talk.global.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class QuestionNoticeService {
    private final ChatroomRepository chatroomRepository;
    private final ChatroomUsersRepository chatroomUsersRepository;
    private final RedisService redisService;
    private final UserClient userClient;
    private final QuestionRepository questionRepository;
    private final KeywordService keywordService;
    private final QuestionNoticeRedisService questionNoticeRedisService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional(readOnly = true)
    public QuestionNoticeResponseDto getQuestionNotice(final long roomId, final int questionNumber, final long senderId) {
        // redis에서 현재 진행상황 있는지 조회
        QuestionNoticeManagementRedisDto dto = questionNoticeRedisService.getCurrentQuestionNoticeDto(roomId);

        // 없으면 새로 만들기
        if (dto == null) {
            Chatroom chatroom = chatroomRepository.findChatroomByChatroomId(roomId);
            var chatroomUsersList = chatroomUsersRepository.findChatroomUsersByChatroom(chatroom);
            if (chatroomUsersList.isEmpty()) {
                throw new CustomException(CustomError.CHATROOM_DOES_NOT_EXIST);
            }
            var userList = userClient.requiredEnterInfo(0, chatroomUsersList.stream()
                    .map(ChatroomUsers::getUserId)
                    .collect(Collectors.toList()))
                .stream()
                .map(QuestionNoticeResponseDto.User::of)
                .collect(Collectors.toUnmodifiableList());

            List<QuestionNoticeManagementRedisDto.QuestionUserMap> tempUserMaps = new ArrayList<>();

            userList.forEach(enterInfo -> {
                List<Long> questionCodes = redisService.findQuestionCode(roomId, enterInfo.getUserId());
                questionCodes.forEach(code -> tempUserMaps.add(QuestionNoticeManagementRedisDto.QuestionUserMap.builder()
                    .userId(enterInfo.getUserId())
                    .questionCode(code)
                    .build()));
            });

            List<QuestionNoticeManagementRedisDto.QuestionUserMap> finalUserMaps = new ArrayList<>();

            // 랜덤하게 섞는 로직
            do {
                int randomIndex = RandomUtils.nextInt(tempUserMaps.size());
                var randomPickedObject = tempUserMaps.get(randomIndex);
                // 랜덤하게 뽑은 질문의 유저가 방금 추가한 유저와 같을 때는 skip. 다시.
                if (!finalUserMaps.isEmpty()
                    && finalUserMaps.get(finalUserMaps.size() - 1).getUserId() == randomPickedObject.getUserId()) {
                    continue;
                }
                finalUserMaps.add(randomPickedObject);
                tempUserMaps.remove(randomPickedObject);
            } while (!CollectionUtils.isEmpty(tempUserMaps));

            dto = QuestionNoticeManagementRedisDto.builder()
                .userList(userList)
                .questionList(finalUserMaps)
                .build();
            questionNoticeRedisService.saveCurrentQuestionStatus(roomId, dto);
        }

        // 들어온 questionNumber가 기존 저장값보다 낮거나 같을때는 현재 저장된 값으로 리턴
        final int currentQuestionNumber = questionNoticeRedisService.getCurrentQuestionNumber(roomId);
        final int questionIndex;
        if (questionNumber <= currentQuestionNumber) {
            questionIndex = currentQuestionNumber - 1;
        } else {
            // questionNumber가 기존값보다 큰 경우 새로 저장
            questionNoticeRedisService.saveCurrentQuestionNumber(roomId, questionNumber);
            messagingTemplate.convertAndSend(
                StompConstants.getRoomEmoticonDestination(roomId),
                EmoticonResponseDto.builder().emoticonCode(0).build()
            );
            questionIndex = questionNumber - 1;
        }

        // questionNumber는 1부터 시작이라 0-based index를 위해 1 빼줌

        var currentUserId = dto.getQuestionList().get(questionIndex).getUserId();
        var speaker = dto.getUserList().stream()
            .filter(enterInfo -> enterInfo.getUserId() == currentUserId)
            .findFirst()
            .orElseThrow(() -> {
                log.error("Redis data validation error.\n" +
                    "Current User Id: {}\n", currentUserId);
                return new CustomException(CustomError.SERVER_ERROR);
            });

        Question currentQuestion = questionRepository.findById(dto.getQuestionList().get(questionIndex).getQuestionCode())
            .orElseThrow(CustomException::new);

        return QuestionNoticeResponseDto.builder()
            .speaker(speaker)
            .userList(dto.getUserList())
            .topic(Topic.builder()
                .keywordName(currentQuestion.getKeyword().getName())
                .questionId(currentQuestion.getQuestionId())
                .questionName(currentQuestion.getContent())
                .depth(keywordService.convertIdIntoDepth(currentQuestion.getQuestionId()))
                .questionGuide(currentQuestion.getGuideList())
                .build())
            .metadata(QuestionNoticeResponseDto.QuestionNoticeMetadata.builder()
                .senderId(senderId)
                .questionNumber(questionIndex + 1)
                .finalQuestionNumber(dto.getQuestionList().size())
                .build())
            .build();
    }

}
