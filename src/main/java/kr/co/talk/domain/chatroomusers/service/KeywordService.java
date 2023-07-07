package kr.co.talk.domain.chatroomusers.service;

import kr.co.talk.domain.chatroom.dto.SocketFlagResponseDto;
import kr.co.talk.domain.chatroomusers.dto.*;
import kr.co.talk.domain.chatroomusers.entity.ChatroomUsers;
import kr.co.talk.domain.chatroomusers.entity.Keyword;
import kr.co.talk.domain.chatroomusers.entity.Question;
import kr.co.talk.domain.chatroomusers.repository.ChatroomUsersRepository;
import kr.co.talk.domain.chatroomusers.repository.KeywordRepository;
import kr.co.talk.domain.chatroomusers.repository.QuestionRepository;
import kr.co.talk.domain.questionnotice.service.QuestionNoticeRedisService;
import kr.co.talk.global.client.UserClient;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import kr.co.talk.global.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final QuestionRepository questionRepository;
    private final ChatroomUsersRepository usersRepository;
    private final QuestionNoticeRedisService questionNoticeRedisService;
    private final RedisService redisService;
    private final UserClient userClient;

    public SocketFlagResponseDto setQuestionWithFlag (KeywordSendDto keywordSendDto) {
        List<Long> keywordCode = keywordSendDto.getKeywordCode();
        List<TopicListDto> responseDto = new ArrayList<>();
        boolean within24Hours = redisService.isWithin24Hours(keywordSendDto.getRoomId(), keywordSendDto.getUserId());
        setUserQuestionList(keywordSendDto.getUserId(), keywordCode, responseDto, within24Hours);

        List<Long> questionCode = responseDto.stream()
                .map(TopicListDto::getQuestionId)
                .collect(Collectors.toList());

        KeywordSetDto keywordSetDto = KeywordSetDto.builder()
                .roomId(keywordSendDto.getRoomId())
                .keywordCode(keywordCode)
                .questionCode(questionCode)
                .build();

        redisService.pushQuestionList(keywordSendDto.getRoomId(), keywordSendDto.getUserId(), keywordSetDto);
        ChatroomUsers user = usersRepository.findChatroomUsersByChatroomIdAndUserId(keywordSendDto.getRoomId(), keywordSendDto.getUserId());
        user.setSocketFlag(4);

        List<SocketFlagResponseDto.TopicListDto> topicListDtoList = new ArrayList<>();
        for (int i = 0; i < keywordCode.size(); i++ ) {
            SocketFlagResponseDto.TopicListDto dto = SocketFlagResponseDto.TopicListDto.builder().keywordId(keywordCode.get(i)).keywordName(keywordRepository.findByKeywordId(keywordCode.get(i)).getName())
                    .questionId(questionCode.get(i)).questionName(questionRepository.findByQuestionId(questionCode.get(i)).getContent()).depth(convertIdIntoDepth(questionCode.get(i))).build();
            topicListDtoList.add(dto);
        }

        usersRepository.save(user);
        return SocketFlagResponseDto.builder().topicList(topicListDtoList).socketFlag(4).build();
    }

    public List<TopicListDto> sendTopicList(TopicListRedisDto listDto) {
        List<Long> keywordCode = listDto.getKeywordList();
        List<Long> questionCode = listDto.getQuestionList();
        List<TopicListDto> responseDto = new ArrayList<>();

        for (int i = 0; i < keywordCode.size(); i++ ) {
            TopicListDto dto = TopicListDto.builder().keywordId(keywordCode.get(i)).keywordName(keywordRepository.findByKeywordId(keywordCode.get(i)).getName())
                    .questionId(questionCode.get(i)).questionName(questionRepository.findByQuestionId(questionCode.get(i)).getContent()).depth(convertIdIntoDepth(questionCode.get(i))).build();
            responseDto.add(dto);
        }

        return responseDto;
    }

    private void setUserQuestionList (long userId, List<Long> keywordCode, List<TopicListDto> responseDto, boolean existingRoom) {

        for (Long code : keywordCode) {
            Keyword keyword = keywordRepository.findByKeywordId(code);
            if (keyword == null) {
                throw new CustomException(CustomError.KEYWORD_DOES_NOT_EXIST);
            }

            List<Question> firstQuestionList = questionRepository.findByKeyword_KeywordId(code);
            if (firstQuestionList == null) {
                throw new CustomException(CustomError.KEYWORD_DOES_NOT_MATCH);
            }

            if (existingRoom) {
                Question randomQuestion = getRandomQuestion(firstQuestionList);
                TopicListDto topics = createTopicListDto(keyword, randomQuestion);
                responseDto.add(topics);
            } else {
                List<Question> filteredQuestions = firstQuestionList.stream()
                        .filter(question -> question.getStatusMap() == 0)
                        .collect(Collectors.toList());

                List<Question> byQuestion = questionRepository.findByKeyword_KeywordIdAndAndStatusMap(code, setUserStatusMap(userId));
                List<Question> addedQuestions = new ArrayList<>(byQuestion);
                if (byQuestion.isEmpty() || !filteredQuestions.isEmpty()) {
                    addedQuestions.addAll(filteredQuestions);
                }

                Question question = getRandomQuestion(addedQuestions);
                TopicListDto topicListDto = createTopicListDto(keyword, question);

                responseDto.add(topicListDto);
            }
        }
    }

    public AllRegisteredDto setQuestionOrder(QuestionCodeDto listDto) {
        boolean registered;
        List<Long> questionCode = redisService.findQuestionCode(listDto.getRoomId(), listDto.getUserId());

        if (!questionCode.containsAll(listDto.getQuestionCodeList())) {
            throw new CustomException(CustomError.QUESTION_ID_NOT_MATCHED);
        } else if (listDto.getQuestionCodeList().stream().distinct().count() != listDto.getQuestionCodeList().size()) {
            throw new CustomException(CustomError.NOT_DISTINCT_QUESTION_LIST);
        }
        Long countValue = redisService.setQuestionCode(listDto.getUserId(), listDto.getRoomId(), listDto);
        List<ChatroomUsers> users = usersRepository.findChatroomUsersByChatroom_ChatroomId(listDto.getRoomId());
        registered = users.size() == countValue;
        ChatroomUsers user = usersRepository.findChatroomUsersByChatroomIdAndUserId(listDto.getRoomId(), listDto.getUserId());
        user.setSocketFlag(5);
        usersRepository.save(user);

        if (registered) {
            for (ChatroomUsers setUser : users) {
                setUser.setSocketFlag(6);
            }
            usersRepository.saveAll(users);
        }
        return AllRegisteredDto.builder().allRegistered(registered).questionNumber(questionNoticeRedisService.getCurrentQuestionNumber(listDto.getRoomId())).build();
    }

    public Integer setUserStatusMap(long userId) {
        String imgCode = userClient.getUserImgCode(userId);
        if (imgCode.equals("ng")) {
            return 1;
        } else if (imgCode.equals("sp")) {
            return 2;
        } else if (imgCode.equals("ha")) {
            return 3;
        } else if (imgCode.equals("fu")) {
            return 4;
        } else if (imgCode.equals("bl")) {
            return 5;
        }
        return 0;
    }

    public String convertIdIntoDepth(long questionId) {
        int lastDigit = (int) (questionId % 10); // 마지막 자리수 추출
        int depth = lastDigit;
        if (lastDigit == 0) {
            depth = 10;
        }
        return depth + "m";
    }


    private Question getRandomQuestion(List<Question> questions) {
        if (questions.isEmpty()) {
            return null;
        }
        int randomIndex = (int) (Math.random() * questions.size());
        return questions.get(randomIndex);
    }

    private TopicListDto createTopicListDto(Keyword keyword, Question question) {
        return TopicListDto.builder()
                .keywordId(keyword.getKeywordId())
                .keywordName(keyword.getName())
                .questionId(question.getQuestionId())
                .questionName(question.getContent())
                .depth(convertIdIntoDepth(question.getQuestionId()))
                .build();
    }
    
    public List<String> getQuestion(List<Long> questionIds){
        return questionRepository.findAllById(questionIds)
                .stream()
                .map(Question::getContent)
                .collect(Collectors.toList());
    }

}
