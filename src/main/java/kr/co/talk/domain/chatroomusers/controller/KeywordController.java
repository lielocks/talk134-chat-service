package kr.co.talk.domain.chatroomusers.controller;

import kr.co.talk.domain.chatroomusers.dto.KeywordSendDto;
import kr.co.talk.domain.chatroomusers.dto.TopicListDto;
import kr.co.talk.domain.chatroomusers.service.KeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/chat")
@RestController
@RequiredArgsConstructor
public class KeywordController {

    private final KeywordService keywordService;

    @PostMapping("/select-keyword")
    public List<TopicListDto> selectKeywordChatroom(@RequestHeader long userId, @RequestBody KeywordSendDto sendDto) {
        return keywordService.sendTopicList(sendDto);
    }
}
