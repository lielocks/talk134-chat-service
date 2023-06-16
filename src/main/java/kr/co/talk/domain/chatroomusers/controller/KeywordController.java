package kr.co.talk.domain.chatroomusers.controller;

import kr.co.talk.domain.chatroomusers.dto.AllRegisteredDto;
import kr.co.talk.domain.chatroomusers.dto.KeywordSendDto;
import kr.co.talk.domain.chatroomusers.dto.QuestionCodeDto;
import kr.co.talk.domain.chatroomusers.dto.TopicListDto;
import kr.co.talk.domain.chatroomusers.service.KeywordService;
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

    @PostMapping("/select-keyword")
    public List<TopicListDto> selectKeywordChatroom(@RequestHeader long userId, @RequestBody KeywordSendDto sendDto) {
        return keywordService.sendTopicList(userId, sendDto);
    }

    @PostMapping("/question-order")
    public AllRegisteredDto questionOrder(@RequestHeader long userId, @RequestBody QuestionCodeDto codeDto) {
        return keywordService.setQuestionOrder(userId, codeDto);
    }
}
