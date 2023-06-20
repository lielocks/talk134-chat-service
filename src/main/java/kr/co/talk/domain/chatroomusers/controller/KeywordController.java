package kr.co.talk.domain.chatroomusers.controller;

import kr.co.talk.domain.chatroom.dto.SocketFlagResponseDto;
import kr.co.talk.domain.chatroomusers.dto.*;
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
    public SocketFlagResponseDto selectKeywordChatroom(@RequestHeader long userId, @RequestBody KeywordSendDto sendDto) {
        return keywordService.setQuestionWithFlag(userId, sendDto);
    }

    @PostMapping("/question-order")
    public AllRegisteredDto questionOrder(@RequestHeader long userId, @RequestBody QuestionCodeDto codeDto) {
        return keywordService.setQuestionOrder(userId, codeDto);
    }

    @GetMapping("/keyword/{roomId}")
    public List<TopicListDto> getKeywordQuestionList(@RequestHeader long userId, @PathVariable long roomId) {
        TopicListRedisDto topicListRedisDto = redisService.returnTopicList(userId, roomId);
        return keywordService.sendTopicList(topicListRedisDto);
    }
}
