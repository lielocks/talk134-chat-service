package kr.co.talk.global.service.redis;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import kr.co.talk.domain.chatroomusers.dto.CountRedisDto;
import kr.co.talk.domain.chatroomusers.dto.KeywordSetDto;
import kr.co.talk.domain.chatroomusers.dto.QuestionCodeDto;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import org.springframework.data.redis.core.*;
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

    public void pushQuestionList(long roomId, long userId, KeywordSetDto keywordSetDto) {
        try {
            String writeValueAsString = objectMapper.writeValueAsString(keywordSetDto);
            String key = roomId + "_" + userId + RedisConstants.QUESTION;
            List<String> list = getList(key);
            log.info("list list list :: {}", list);
            if (list == null || list.isEmpty()) {
                opsForList.leftPush(key, writeValueAsString);
            } else {
                throw new CustomException(CustomError.QUESTION_ALREADY_REGISTERED);
            }

        } catch (JsonProcessingException e) {
            log.error("json parse error", e);
            throw new RuntimeException(e);
        }
    }

    public void setQuestionCode(long userId, long roomId, QuestionCodeDto listDto) {
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

    public List<Long> findQuestionCode(long roomId, long userId) {
        try {
            String key = roomId + "_" + userId + RedisConstants.QUESTION;
            List<String> list = getList(key);
            KeywordSetDto keywordDtoValue = objectMapper.readValue(list.get(0), KeywordSetDto.class);
            List<Long> questionCode = keywordDtoValue.getQuestionCode();
            return questionCode;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Long> findKeywordCode(long roomId, long userId) {
        try {
            String key = roomId + "_" + userId + RedisConstants.QUESTION;
            List<String> list = getList(key);
            KeywordSetDto keywordDtoValue = objectMapper.readValue(list.get(0), KeywordSetDto.class);
            List<Long> keywordCode = keywordDtoValue.getKeywordCode();
            return keywordCode;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * TODO //expire timeOut -> 대화 마감 알림 소켓에서 status 퇴장으로 set한 시간 - 대화방 입장한 시간
     */
    public void pushUserChatRoom(String userId, String roomId) throws CustomException {
        String key = userId + RedisConstants.CHATROOM;
        valueOps.setIfAbsent(key, roomId, Duration.ofMinutes(10));
        redisTemplate.exec(); // redis transaction commit
    }

    public void getRegisteredCount(long userId, String roomId) {
        String countKey = roomId + RedisConstants.COUNT;
        String listKey = roomId + "_" + userId + RedisConstants.QUESTION;

        List<String> countValues = getList(countKey);
        CountRedisDto countDto;
        try {
            if (countValues != null && !countValues.isEmpty()) {
                countDto = objectMapper.readValue(countValues.get(0), CountRedisDto.class);
            } else {
                countDto = new CountRedisDto(userId, Long.parseLong(roomId), 1);
            }

            int count = countDto.getCount();
            log.info("count :: {}", count);
            if (getList(listKey).size() > 0) {
                count++;
            }

            countDto.setCount(count);
            if (countDto.getRoomId() != Long.parseLong(roomId) && countDto.getUserId() != userId) {
                pushList(countKey, countDto);
            }

        } catch (JsonProcessingException e) {
            log.error("json parse error", e);
            throw new RuntimeException(e);
        }
    }

}
