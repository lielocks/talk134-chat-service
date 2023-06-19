package kr.co.talk.domain.chatroom.service;

import java.time.LocalDateTime;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.talk.global.constants.KafkaConstants;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatRoomSender {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    // private final ChatRedisService chatRedisService;

    public void sendEndChatting(long roomId, long userId) {
        KafkaEndChatroomDTO chatroomDTO = KafkaEndChatroomDTO.builder()
                .roomId(roomId)
                .userId(userId)
                .localDateTime(LocalDateTime.now())
                .build();

        String jsonInString = "";
        try {
            jsonInString = objectMapper.writeValueAsString(chatroomDTO);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        
        log.info("roomId :: {} end message send ~ ", roomId);
       
        
        ListenableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(KafkaConstants.TOPIC_END_CHATTING,
                        jsonInString);
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

            @Override
            public void onSuccess(SendResult<String, String> result) {
                RecordMetadata recordMetadata = result.getRecordMetadata();
                log.info("sent topic=[{}] roodId [{}] with offset=[{}]", recordMetadata.topic(),
                        roomId,
                        recordMetadata.offset());
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("unable to send message roomId=[{}] due to : {}", roomId,
                        ex.getMessage());
                throw new CustomException(CustomError.END_CHATROOM_SAVE_ERROR);
            }
        });
    }


    @Builder
    @Data
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    private static class KafkaEndChatroomDTO {
        private long roomId;
        private long userId;
        private LocalDateTime localDateTime;
    }
}
