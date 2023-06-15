package kr.co.talk.domain.chatroomusers.service;

import kr.co.talk.domain.chatroomusers.dto.*;
import kr.co.talk.domain.chatroomusers.entity.Keyword;
import kr.co.talk.domain.chatroomusers.entity.Question;
import kr.co.talk.domain.chatroomusers.repository.KeywordRepository;
import kr.co.talk.domain.chatroomusers.repository.QuestionRepository;
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
    private final RedisService redisService;
    private final UserClient userClient;

    public List<TopicListDto> sendTopicList(long userId, KeywordSendDto keywordSendDto) {
        List<Long> keywordCode = keywordSendDto.getKeywordCode();
        List<TopicListDto> responseDto = new ArrayList<>();

        for (Long code : keywordCode) {
            Keyword keyword = keywordRepository.findByKeywordId(code);
            if (keyword == null) {
                throw new CustomException(CustomError.KEYWORD_DOES_NOT_EXIST);
            }

            List<Question> questions = questionRepository.findByKeyword_KeywordId(code);
            if (questions == null) {
                throw new CustomException(CustomError.KEYWORD_DOES_NOT_MATCH);
            }

            List<Question> filteredQuestions = questions.stream()
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

        List<Long> questionCode = responseDto.stream()
                .map(TopicListDto::getQuestionId)
                .collect(Collectors.toList());

        KeywordSetDto keywordSetDto = KeywordSetDto.builder()
                .roomId(keywordSendDto.getRoomId())
                .keywordCode(keywordCode)
                .questionCode(questionCode)
                .build();
        redisService.pushQuestionList(keywordSendDto.getRoomId(), userId, keywordSetDto);
        return responseDto;
    }

    public void setQuestionOrder(long userId, QuestionCodeDto listDto) {
        redisService.setQuestionCode(userId, listDto.getRoomId(), listDto);
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
                .keyword(keyword.getName())
                .questionId(question.getQuestionId())
                .questionName(question.getContent())
                .depth(convertIdIntoDepth(question.getQuestionId()))
                .build();
    }

}
