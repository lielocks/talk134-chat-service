package kr.co.talk.domain.chatroom.controller;

import kr.co.talk.domain.chatroom.dto.ChatEnterDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChatRedisController {
    private final ReactiveRedisOperations<String, ChatEnterDto> reactiveTemplate;

    private final ReactiveRedisMessageListenerContainer reactiveMsgListenerContainer;

    @GetMapping("/message/coffees")
    public Flux<String> receiveCoffeeMessages() {
        return reactiveMsgListenerContainer
                .receive(topic)
                .map(ReactiveSubscription.Message::getMessage)
                .map(msg -> {
                    LOG.info("New Message received: '" + msg.toString() + "'.");
                    return msg.toString() + "\n";
                });
    }
}
}
