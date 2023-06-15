package kr.co.talk.domain.chatroomusers.controller;

import kr.co.talk.domain.chatroomusers.dto.KeywordSendDto;
import kr.co.talk.domain.chatroomusers.dto.QuestionCodeDto;
import kr.co.talk.domain.chatroomusers.dto.TopicListDto;
import kr.co.talk.domain.chatroomusers.service.KeywordService;
import kr.co.talk.global.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/chat")
@RestController
@RequiredArgsConstructor
public class KeywordController {

    private final KeywordService keywordService;
    private final RedisService redisService;

    @PostMapping("/select-keyword")
    public List<TopicListDto> selectKeywordChatroom(@RequestHeader long userId, @RequestBody KeywordSendDto sendDto) {
        return keywordService.sendTopicList(userId, sendDto);
    }

    @PostMapping("/question-order")
    public void questionOrder(@RequestHeader long userId, @RequestBody QuestionCodeDto codeDto) {
        keywordService.setQuestionOrder(userId, codeDto);
    }
}
