package kr.co.talk.domain.questionnotice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.talk.domain.questionnotice.dto.QuestionNoticeManagementRedisDto;
import kr.co.talk.global.constants.RedisConstants;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class QuestionNoticeRedisService {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private boolean hasKey(long roomId) {
        return Boolean.TRUE == stringRedisTemplate.hasKey(getQuestionNumberKey(roomId));
    }

    private String getQuestionNumberKey(long roomId) {
        return String.format("%s_%s", RedisConstants.QUESTION_NUMBER, roomId);
    }

    private String getQuestionKey(long roomId) {
        return String.format("%s_%s", roomId, RedisConstants.QUESTION_NOTICE);
    }

    public void saveCurrentQuestionStatus(long roomId, QuestionNoticeManagementRedisDto dto) {
        try {
            String item = objectMapper.writeValueAsString(dto);
            stringRedisTemplate.opsForValue().set(getQuestionKey(roomId), item);
        } catch (JsonProcessingException e) {
            log.error("Error occurred during writing value: {} ", dto);
            throw new CustomException(CustomError.SERVER_ERROR);
        }
    }

    public void saveCurrentQuestionNumber(long roomId, int questionNumber) {
        stringRedisTemplate.opsForValue().set(getQuestionNumberKey(roomId), String.valueOf(questionNumber));
    }

    public QuestionNoticeManagementRedisDto getCurrentQuestionNoticeDto(long roomId) {
        String value = stringRedisTemplate.opsForValue().get(getQuestionKey(roomId));
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.readValue(value, QuestionNoticeManagementRedisDto.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 현재 저장된 questionNumber를 리턴
     * @param roomId room id
     * @return 이 채팅방의 현재 questionNumber. 없으면 1로 세팅 후 1을 리턴.
     */
    public int getCurrentQuestionNumber(long roomId) {
        if (!hasKey(roomId)) {
            saveCurrentQuestionNumber(roomId, 1);
            return 1;
        }
        return Integer.parseInt(stringRedisTemplate.opsForValue().get(getQuestionNumberKey(roomId)));
    }

    public void deleteQuestionNumber(long roomId) {
        stringRedisTemplate.delete(getQuestionNumberKey(roomId));
    }

    public void deleteQuestionManagementDto(long roomId) {
        stringRedisTemplate.delete(getQuestionKey(roomId));
    }
}
