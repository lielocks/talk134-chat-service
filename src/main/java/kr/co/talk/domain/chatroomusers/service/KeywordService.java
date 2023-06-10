package kr.co.talk.domain.chatroomusers.service;

import kr.co.talk.domain.chatroomusers.dto.KeywordSendDto;
import kr.co.talk.domain.chatroomusers.dto.TopicListDto;
import kr.co.talk.domain.chatroomusers.entity.Keyword;
import kr.co.talk.domain.chatroomusers.entity.Question;
import kr.co.talk.domain.chatroomusers.repository.KeywordRepository;
import kr.co.talk.domain.chatroomusers.repository.QuestionRepository;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final QuestionRepository questionRepository;

    public List<TopicListDto> sendTopicList(KeywordSendDto keywordSendDto) {
        List<Long> keywordCode = keywordSendDto.getKeywordCode();
        List<Long> questionCode = keywordSendDto.getQuestionCode();
        List<TopicListDto> responseDto = new ArrayList<>();

        for (int i = 0; i < keywordCode.size(); i++) {
            Question byQuestion = questionRepository.findByQuestionIdAndKeyword_KeywordId(questionCode.get(i), keywordCode.get(i));
            if (byQuestion == null) {
                throw new CustomException(CustomError.KEYWORD_DOES_NOT_EXIST);
            }
            Keyword keyword = keywordRepository.findByKeywordId(keywordCode.get(i));

            TopicListDto topicListDto = new TopicListDto();
            topicListDto.setKeyword(keyword.getName());
            topicListDto.setQuestionName(byQuestion.getContent());
            topicListDto.setDepth(keyword.getDepth());

            responseDto.add(topicListDto);
        }

        return responseDto;
    }

}
