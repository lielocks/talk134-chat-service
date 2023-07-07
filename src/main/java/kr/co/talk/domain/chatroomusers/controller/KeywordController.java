package kr.co.talk.domain.chatroomusers.controller;

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


    @GetMapping("/keyword/{roomId}")
    public List<TopicListDto> getKeywordQuestionList(@RequestHeader long userId, @PathVariable long roomId) {
        TopicListRedisDto topicListRedisDto = redisService.returnTopicList(userId, roomId);
        return keywordService.sendTopicList(topicListRedisDto);
    }
    
    @GetMapping("/keyword/name/{questionIds}")
    public List<String> keywordName(@PathVariable("questionIds") List<Long> questionIds){
        return keywordService.getQuestion(questionIds);
    }
}
