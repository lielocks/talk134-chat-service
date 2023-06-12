package kr.co.talk.global.service.redis;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import kr.co.talk.domain.chatroomusers.dto.KeywordSendDto;
import kr.co.talk.domain.chatroomusers.dto.KeywordSetDto;
import kr.co.talk.domain.chatroomusers.dto.QuestionCodeDto;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.talk.domain.chatroom.dto.ChatroomNoticeDto;
import kr.co.talk.domain.chatroom.dto.RoomEmoticon;
import kr.co.talk.global.constants.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, String> chatroomNoticeDtoRedisTemplate;
    private final ObjectMapper objectMapper;

    private ValueOperations<String, String> valueOps;
    private ListOperations<String, String> opsForList;
    private HashOperations<String, String, String> opsForNoticeMap;

    @PostConstruct
    public void init() {
        valueOps = stringRedisTemplate.opsForValue();
        opsForList = redisTemplate.opsForList();
        opsForNoticeMap = chatroomNoticeDtoRedisTemplate.opsForHash();
    }

    /**
     * get value
     * 
     * @param key
     * @return
     */
    public String getValues(String key) {
        return valueOps.get(key);
    }

    /**
     * set value with timeout
     *
     * @param key
     * @param value
     * @param timeout
     */
    public void setValuesWithTimeout(String key, String value, long timeout) {
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofMillis(timeout));
    }

    public void pushList(String key, Object value) {
        try {
            String item = objectMapper.writeValueAsString(value);
            opsForList.leftPush(key, item);
        } catch (JsonProcessingException e) {
            log.error("json parse exception , key is :: {}, value is :: {}", key, value, e);
            throw new RuntimeException(e);
        }
    }

    public List<String> getList(String key) {
        return opsForList.size(key) == 0 ? new ArrayList<>() : opsForList.range(key, 0, -1);
    }

    public List<RoomEmoticon> getEmoticonList(long chatroomId) {
        String key = chatroomId + RedisConstants.ROOM_EMOTICON;
        List<String> emoticonList =
                opsForList.size(key) == 0 ? new ArrayList<>() : opsForList.range(key, 0, -1);

        return emoticonList.stream().map(s -> {
            try {
                return objectMapper.readValue(s, RoomEmoticon.class);
            } catch (JsonProcessingException e) {
                log.error("json parse error", e);
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

    }

    /**
     * 채팅방이 생성될때 redis에 저장 생성되고나서 5분전, 끝날때 알림
     * 
     * @param key
     * @param chatroomNoticeDto
     */
    public void pushNoticeMap(String roomId, ChatroomNoticeDto chatroomNoticeDto) {
        try {
            String writeValueAsString = objectMapper.writeValueAsString(chatroomNoticeDto);
            opsForNoticeMap.put(RedisConstants.ROOM_NOTICE, roomId, writeValueAsString);
        } catch (JsonProcessingException e) {
            log.error("json parse error", e);
            throw new RuntimeException(e);
        }
    }

    public Map<String, ChatroomNoticeDto> getChatroomNoticeEntry() {
        Map<String, String> entries = opsForNoticeMap.entries(RedisConstants.ROOM_NOTICE);
        return entries.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(),
                e -> {
                    try {
                        return objectMapper.readValue(e.getValue(), ChatroomNoticeDto.class);
                    } catch (JsonProcessingException e1) {
                        log.error("json parse error", e);
                    }
                    return null;
                }));
    }

    public void deleteChatroomNotice(String roomId) {
        opsForNoticeMap.delete(RedisConstants.ROOM_NOTICE, roomId);
    }

    public void pushQuestionList(String roomId, String userId, KeywordSetDto keywordSetDto) {
        try {
            String writeValueAsString = objectMapper.writeValueAsString(keywordSetDto);
            String key = roomId + "_" + userId + RedisConstants.QUESTION;
            opsForList.leftPush(key, writeValueAsString);
        } catch (JsonProcessingException e) {
            log.error("json parse error", e);
            throw new RuntimeException(e);
        }
    }

    public void setQuestionCode(String userId, String roomId, QuestionCodeDto listDto) {
        try {
            String key = roomId + "_" + userId + RedisConstants.QUESTION;
            List<String> valueList = getList(key);
            KeywordSetDto keywordDtoValue = objectMapper.readValue(valueList.get(0), KeywordSetDto.class);

            List<Long> firstCode = keywordDtoValue.getQuestionCode();
            if (listDto.getQuestionCodeList().size() != firstCode.size()) {
                throw new CustomException(CustomError.QUESTION_LIST_SIZE_MISMATCH);
            }
            for (int i = 0; i < listDto.getQuestionCodeList().size(); i++) {
                firstCode.set(i, listDto.getQuestionCodeList().get(i));
            }

            valueList.set(0, objectMapper.writeValueAsString(keywordDtoValue));
            redisTemplate.opsForList().set(key, 0, valueList.get(0));
        } catch (JsonProcessingException e) {
            log.error("json parse error", e);
            throw new RuntimeException(e);
        }
    }

}
