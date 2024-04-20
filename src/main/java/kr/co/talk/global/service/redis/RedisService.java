package kr.co.talk.global.service.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.talk.domain.chatroom.dto.RoomEmoticon;
import kr.co.talk.domain.chatroomusers.dto.KeywordSetDto;
import kr.co.talk.domain.chatroomusers.dto.QuestionCodeDto;
import kr.co.talk.domain.chatroomusers.dto.TopicListRedisDto;
import kr.co.talk.domain.chatroomusers.entity.ChatroomUsers;
import kr.co.talk.global.constants.RedisConstants;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Integer> integerRedisTemplate;
    private final ObjectMapper objectMapper;

    private ValueOperations<String, String> valueOps;
    private ValueOperations<String, Integer> integerValueOps;
    private ListOperations<String, String> opsForList;
    private HashOperations<String, String, String> opsForMap;

    @PostConstruct
    public void init() {
        valueOps = stringRedisTemplate.opsForValue();
        integerValueOps = integerRedisTemplate.opsForValue();
        opsForList = redisTemplate.opsForList();
        opsForMap = redisTemplate.opsForHash();
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


    public void pushMap(String key, String fieldKey, Object value) {
        try {
            String writeValueAsString = objectMapper.writeValueAsString(value);
            opsForMap.put(key, fieldKey, writeValueAsString);
        } catch (JsonProcessingException e) {
            log.error("json parse error", e);
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> getEntry(String key, Class<?> clazz) {
        Map<String, String> entries = opsForMap.entries(key);
        return entries.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(),
                e -> {
                    try {
                        return objectMapper.readValue(e.getValue(), clazz);
                    } catch (JsonProcessingException e1) {
                        log.error("json parse error", e);
                    }
                    return null;
                }));
    }
    
    public Object getValueOfMap(String key, String subKey, Class<?> clazz) {
		try {
			return objectMapper.readValue(opsForMap.get(key, subKey), clazz);
		} catch (JsonProcessingException e) {
			log.error("json parse error", e);
		}
		return null;

	}

    public void deleteMap(String key, String fieldKey) {
        opsForMap.delete(key, fieldKey);
    }

    public void pushQuestionList(long roomId, long userId, KeywordSetDto keywordSetDto) {
        try {
            String key = roomId + "_" + userId + RedisConstants.QUESTION;
            if (getValues(key) == null || getValues(key).isEmpty()) {
                int count = keywordSetDto.getRegisteredQuestionOrder();
                count += 1;
                keywordSetDto.setRegisteredQuestionOrder(count);
                String writeValueAsString = objectMapper.writeValueAsString(keywordSetDto);
                valueOps.set(key, writeValueAsString);
            }

            else if (findRegisteredCount(roomId, userId) == 1) {
                String valueList = getValues(key);
                KeywordSetDto keywordDtoValue = objectMapper.readValue(valueList, KeywordSetDto.class);

                int counted = keywordDtoValue.getRegisteredQuestionOrder();
                counted += 1;
                keywordDtoValue.setRegisteredQuestionOrder(counted);
                keywordDtoValue.setQuestionCode(keywordSetDto.getQuestionCode());
                keywordDtoValue.setKeywordCode(keywordSetDto.getKeywordCode());
                String writeValueAsString = objectMapper.writeValueAsString(keywordDtoValue);

                valueOps.set(key, writeValueAsString);
            }

            else if (findRegisteredCount(roomId, userId) == 2) {
                String valueList = getValues(key);
                KeywordSetDto keywordDtoValue = objectMapper.readValue(valueList, KeywordSetDto.class);

                keywordDtoValue.setRegisteredQuestionOrder(keywordDtoValue.getRegisteredQuestionOrder());
                keywordDtoValue.setQuestionCode(keywordDtoValue.getQuestionCode());
                keywordDtoValue.setKeywordCode(keywordDtoValue.getKeywordCode());

                String writeValueAsString = objectMapper.writeValueAsString(keywordDtoValue);
                valueOps.set(key, writeValueAsString);
                throw new CustomException(CustomError.QUESTION_ALREADY_REGISTERED);
            }

        } catch (JsonProcessingException e) {
            log.error("json parse error", e);
            throw new RuntimeException(e);
        }
    }

    public Long setQuestionCode(long userId, long roomId, QuestionCodeDto listDto) {
        try {
            String key = roomId + "_" + userId + RedisConstants.QUESTION;
            String countKey = roomId + "_" + userId + RedisConstants.COUNT;
            String roomCountKey = roomId + RedisConstants.COUNT;
            String valueList = getValues(key);

            KeywordSetDto keywordDtoValue = objectMapper.readValue(valueList, KeywordSetDto.class);
            List<Long> firstCode = keywordDtoValue.getQuestionCode();

            if (listDto.getQuestionCodeList().size() != firstCode.size()) {
                throw new CustomException(CustomError.QUESTION_LIST_SIZE_MISMATCH);
            }
            for (int i = 0; i < listDto.getQuestionCodeList().size(); i++) {
                firstCode.set(i, listDto.getQuestionCodeList().get(i));
            }
            keywordDtoValue.setRegisteredQuestionOrder(keywordDtoValue.getRegisteredQuestionOrder() + 1);

            valueOps.set(key, objectMapper.writeValueAsString(keywordDtoValue));

            if (Objects.equals(Boolean.FALSE, integerRedisTemplate.hasKey(countKey))) {
                integerValueOps.increment(countKey);
                return integerValueOps.increment(roomCountKey);
            } else {
                throw new CustomException(CustomError.QUESTION_ORDER_CHANCE_ONCE);
            }
        } catch (JsonProcessingException e) {
            log.error("json parse error", e);
            throw new RuntimeException(e);
        }
    }

    public List<Long> findQuestionCode(long roomId, long userId) {
        try {
            String key = roomId + "_" + userId + RedisConstants.QUESTION;
            String values = getValues(key);
            KeywordSetDto keywordDtoValue = objectMapper.readValue(values, KeywordSetDto.class);
            List<Long> questionCode = keywordDtoValue.getQuestionCode();

            return questionCode;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Long> findKeywordCode(long roomId, long userId) {
        try {
            String key = roomId + "_" + userId + RedisConstants.QUESTION;
            String values = getValues(key);
            KeywordSetDto keywordDtoValue = objectMapper.readValue(values, KeywordSetDto.class);

            List<Long> keywordCode = keywordDtoValue.getKeywordCode();
            return keywordCode;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Integer findRegisteredCount (long roomId, long userId) {
        try {
            String key = roomId + "_" + userId + RedisConstants.QUESTION;
            String values = getValues(key);
            KeywordSetDto keywordDtoValue = objectMapper.readValue(values, KeywordSetDto.class);
            return keywordDtoValue.getRegisteredQuestionOrder();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * CHATROOM timeout -> 모든 유저 feedback 등록까지 완료 후 remove consumer 에서 expire
     */
    public void pushUserChatRoom(long userId, long roomId) throws CustomException {
        String key = userId + RedisConstants.CHATROOM;
        valueOps.setIfAbsent(key, String.valueOf(roomId));
    }

    public void roomCreateTime (long roomId, long userId) {
        String timeKey = userId + "_" + roomId + RedisConstants.TIME;
        valueOps.setIfAbsent(timeKey, String.valueOf(System.currentTimeMillis()), Duration.ofDays(1));
        redisTemplate.multi(); // chat service 에서 pushUserChatRoom 후 multi 로 transaction 열어줌
        redisTemplate.exec();
    }

    public void chatRoomCreateTime(long roomId) {
        String timeKey = roomId + RedisConstants.TIME;
        valueOps.setIfAbsent(timeKey, String.valueOf(System.currentTimeMillis()));
    }

    public boolean findChatRoomTime(long roomId) {
        String timeKey = roomId + RedisConstants.TIME;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(timeKey));
    }

    public boolean isWithin24Hours(long roomId, long userId) {
        String timeKey = userId + "_" + roomId + RedisConstants.TIME;
        String timeValueStr = getValues(timeKey);
        if (timeValueStr == null || timeValueStr.isEmpty()) {
            return false;
        }

        try {
            long timeValue = Long.parseLong(timeValueStr);
            long currentTime = System.currentTimeMillis() / 1000; // 현재 시간
            long storedTimeSeconds = timeValue / 1000; // room 생성된 시간
            long timeDifference = currentTime - storedTimeSeconds;
            long twentyFourHoursInSeconds = 24 * 60 * 60;

            return timeDifference <= twentyFourHoursInSeconds;
        } catch (NumberFormatException e) {
            return false;
        }

    }

    public boolean isWithin10Minutes(List<ChatroomUsers> chatroomUsers) {
        long earliestTimeValue = Long.MAX_VALUE;

        for (ChatroomUsers user : chatroomUsers) {
            long roomId = user.getChatroom().getChatroomId();
            long userId = user.getUserId();
            String timeKey = userId + "_" + roomId + RedisConstants.TIME;
            String timeValueStr = getValues(timeKey);

            if (timeValueStr != null && !timeValueStr.isEmpty()) {
                long timeValue = Long.parseLong(timeValueStr);
                earliestTimeValue = Math.min(earliestTimeValue, timeValue);
            }
        }

        try {
            long storedTimeSeconds = earliestTimeValue / 1000; // room 생성된 시간
            long currentTime = System.currentTimeMillis() / 1000; // 현재 시간
            long timeDifference = currentTime - storedTimeSeconds;
            long tenMinutesInSeconds = 60 * 10;
            log.info("timeDifference >= tenMinutesInSeconds :: {}", timeDifference >= tenMinutesInSeconds);

            return timeDifference >= tenMinutesInSeconds;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean flagSetWithin10Minutes(List<ChatroomUsers> chatroomUsers) {
        long earliestTimeValue = Long.MAX_VALUE;

        for (ChatroomUsers user : chatroomUsers) {
            long roomId = user.getChatroom().getChatroomId();
            String timeKey = roomId + RedisConstants.TIME;
            String timeValueStr = getValues(timeKey);

            if (timeValueStr != null && !timeValueStr.isEmpty()) {
                long timeValue = Long.parseLong(timeValueStr);
                earliestTimeValue = Math.min(earliestTimeValue, timeValue);
            }
        }

        try {
            long flagSetTimeSeconds = earliestTimeValue / 1000; // chatroom users 중 socketFlag 2로 처음 set 된 시간
            long currentTime = System.currentTimeMillis() / 1000; // 현재 시간
            long flagTimeDifference = currentTime - flagSetTimeSeconds;
            long tenMinutesInSeconds = 60 * 10;
            log.info("flagTimeDifference >= tenMinutesInSeconds :: {}", flagTimeDifference >= tenMinutesInSeconds);

            return flagTimeDifference >= tenMinutesInSeconds;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    public TopicListRedisDto returnTopicList(long userId, long roomId) {
        String values = getValues(roomId + "_" + userId + RedisConstants.QUESTION);
        try {
            KeywordSetDto keywordDtoValue = objectMapper.readValue(values, KeywordSetDto.class);
            TopicListRedisDto listRedisDto = TopicListRedisDto.builder().questionList(keywordDtoValue.getQuestionCode()).keywordList(keywordDtoValue.getKeywordCode()).build();
            return listRedisDto;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    
    public void deleteCountAndChatroomKey(long roomId) {
        String key = "*" + RedisConstants.CHATROOM;
        String countKey = roomId + "_" + "*" + RedisConstants.COUNT;
        String roomCountKey = roomId + RedisConstants.COUNT;
        String roomTimeKey = roomId + RedisConstants.TIME;

        if (Boolean.TRUE.equals(integerRedisTemplate.hasKey(roomCountKey))) {
            integerRedisTemplate.delete(roomCountKey);
        }
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(roomTimeKey))) {
            stringRedisTemplate.delete(roomTimeKey);
        }

        Set<String> countKeys = integerRedisTemplate.keys(countKey);
        if (countKeys != null) {
            integerRedisTemplate.delete(countKeys);
        }

        Set<String> chatroomKeys = redisTemplate.keys(key);
        if (chatroomKeys != null) {
            redisTemplate.delete(chatroomKeys);
        }
    }
    
    public void deleteQuestionKey(String roomId, long userId) {
  	  String questionKey = roomId + "_" + userId + RedisConstants.QUESTION;
  	  redisTemplate.delete(questionKey);
  }
}
