package kr.co.talk.domain.questionnotice.service;

import kr.co.talk.domain.chatroom.model.Chatroom;
import kr.co.talk.domain.chatroom.repository.ChatroomRepository;
import kr.co.talk.domain.chatroomusers.entity.ChatroomUsers;
import kr.co.talk.domain.chatroomusers.entity.Question;
import kr.co.talk.domain.chatroomusers.repository.ChatroomUsersRepository;
import kr.co.talk.domain.chatroomusers.repository.QuestionRepository;
import kr.co.talk.domain.chatroomusers.service.KeywordService;
import kr.co.talk.domain.questionnotice.dto.QuestionNoticeManagementRedisDto;
import kr.co.talk.domain.questionnotice.dto.QuestionNoticeResponseDto;
import kr.co.talk.domain.questionnotice.dto.QuestionNoticeResponseDto.Topic;
import kr.co.talk.global.client.UserClient;
import kr.co.talk.global.constants.RedisConstants;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import kr.co.talk.global.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class QuestionNoticeService {
    private final ChatroomRepository chatroomRepository;
    private final ChatroomUsersRepository chatroomUsersRepository;
    private final RedisService redisService;
    private final UserClient userClient;
    private final QuestionRepository questionRepository;
    private final KeywordService keywordService;

    @Transactional(readOnly = true)
    public QuestionNoticeResponseDto getQuestionNotice(long roomId) {
        // redis에서 현재 진행상황 있는지 조회
        QuestionNoticeManagementRedisDto dto = redisService.getCurrentQuestionNoticeDto(getQuestionKey(roomId));

        // 없으면 새로 만들기
        if (dto == null) {
            Chatroom chatroom = chatroomRepository.findChatroomByChatroomId(roomId);
            var chatroomUsersList = chatroomUsersRepository.findChatroomUsersByChatroom(chatroom);
            if (chatroomUsersList.isEmpty()) {
                throw new CustomException(CustomError.CHATROOM_DOES_NOT_EXIST);
            }
            var userList = userClient.requiredEnterInfo(0, chatroomUsersList.stream()
                    .map(ChatroomUsers::getUserId)
                    .collect(Collectors.toList()));
            // 순서 랜덤으로 변경
            Collections.shuffle(userList);
            dto = QuestionNoticeManagementRedisDto.builder()
                    .currentSpeakerIndex(0)
                    .currentQuestionIndex(0)
                    .speakerQueue(userList)
                    .build();
        } else {
            if (dto.isFinalQuestion()) {
                if (dto.isFinalSpeaker()) {
                    throw new CustomException(CustomError.ALREADY_FINISHED);
                }
                dto.incrementCurrentSpeakerIndex();
                dto.resetQuestionIndex();
            } else {
                dto.incrementCurrentQuestionIndex();
            }
        }

        saveCurrentQuestionStatus(roomId, dto);

        var speaker = dto.getSpeakerQueue().get(dto.getCurrentSpeakerIndex());
        List<Long> questionCodes = redisService.findQuestionCode(roomId, speaker.getUserId());
        Question currentQuestion = questionRepository.findById(questionCodes.get(dto.getCurrentQuestionIndex()))
                .orElseThrow(CustomException::new);

        return QuestionNoticeResponseDto.builder()
                .speaker(speaker)
                .userList(dto.getSpeakerQueue())
                .topic(Topic.builder()
                        .keyword(currentQuestion.getKeyword().getName())
                        .questionCode(currentQuestion.getQuestionId())
                        .questionName(currentQuestion.getContent())
                        .depth(keywordService.convertIdIntoDepth(currentQuestion.getQuestionId()))
                        .questionGuide(currentQuestion.getGuideList())
                        .build())
                .questionCount(dto.getCurrentQuestionIndex() + 1)
                .endFlag(dto.isFinalSpeaker() && dto.isFinalQuestion())
                .build();
    }

    private String getQuestionKey(long roomId) {
        return String.format("%s_%s", roomId, RedisConstants.QUESTION_NOTICE);
    }

    private void saveCurrentQuestionStatus(long roomId, QuestionNoticeManagementRedisDto dto) {
        redisService.saveObject(getQuestionKey(roomId), dto);
    }
}
