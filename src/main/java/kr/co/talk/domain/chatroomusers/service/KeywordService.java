package kr.co.talk.domain.chatroomusers.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.talk.domain.chatroomusers.dto.KeywordSendDto;
import kr.co.talk.domain.chatroomusers.dto.KeywordSetDto;
import kr.co.talk.domain.chatroomusers.dto.QuestionCodeDto;
import kr.co.talk.domain.chatroomusers.dto.TopicListDto;
import kr.co.talk.domain.chatroomusers.entity.Keyword;
import kr.co.talk.domain.chatroomusers.entity.Question;
import kr.co.talk.domain.chatroomusers.repository.KeywordRepository;
import kr.co.talk.domain.chatroomusers.repository.QuestionRepository;
import kr.co.talk.global.constants.RedisConstants;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import kr.co.talk.global.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final QuestionRepository questionRepository;
    private final RedisService redisService;

    public List<TopicListDto> sendTopicList(long userId, KeywordSendDto keywordSendDto) {
        List<Long> keywordCode = keywordSendDto.getKeywordCode();
        List<TopicListDto> responseDto = new ArrayList<>();

        for (int i = 0; i < keywordCode.size(); i++) {
            Keyword keyword = keywordRepository.findByKeywordId(keywordCode.get(i));
            if (keyword == null) {
                throw new CustomException(CustomError.KEYWORD_DOES_NOT_EXIST);
            }
            List<Question> byQuestion = questionRepository.findByKeyword_KeywordId(keywordCode.get(i));
            if (byQuestion == null) {
                throw new CustomException(CustomError.KEYWORD_DOES_NOT_MATCH);
            }

            int randomIndex = (int) (Math.random() * byQuestion.size());
            Question question = byQuestion.get(randomIndex);

            TopicListDto topicListDto =
                    TopicListDto.builder().keyword(keyword.getName()).questionId(question.getQuestionId()).questionName(question.getContent()).depth(keyword.getDepth()).build();

            responseDto.add(topicListDto);
        }

        List<Long> questionCode = responseDto.stream()
                .map(TopicListDto::getQuestionId)
                .collect(Collectors.toList());

        KeywordSetDto keywordSetDto = KeywordSetDto.builder().roomId(keywordSendDto.getRoomId()).keywordCode(keywordCode).questionCode(questionCode).build();
        redisService.pushQuestionList(String.valueOf(keywordSendDto.getRoomId()), String.valueOf(userId), keywordSetDto);
        return responseDto;
    }

    public void setQuestionOrder(long userId, QuestionCodeDto listDto) {
        redisService.setQuestionCode(String.valueOf(userId), String.valueOf(listDto.getRoomId()), listDto);
    }

}
