package kr.co.talk.domain.questionnotice.service;

import kr.co.talk.global.constants.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class QuestionNoticeRedisService {
    private final StringRedisTemplate stringRedisTemplate;

    private boolean hasKey(long roomId) {
        return Boolean.TRUE == stringRedisTemplate.hasKey(getQuestionNumberKey(roomId));
    }

    private String getQuestionNumberKey(long roomId) {
        return String.format("%s_%s", RedisConstants.QUESTION_NUMBER, roomId);
    }

    public void saveCurrentQuestionNumber(long roomId, int questionNumber) {
        stringRedisTemplate.opsForValue().set(getQuestionNumberKey(roomId), String.valueOf(questionNumber));
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
}
