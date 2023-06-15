package kr.co.talk.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.List;

import kr.co.talk.domain.chatroom.dto.RoomEmoticon;
import kr.co.talk.domain.chatroom.model.EmoticonCode;
import kr.co.talk.domain.chatroomusers.dto.KeywordSetDto;
import kr.co.talk.global.constants.RedisConstants;
import kr.co.talk.global.service.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@Slf4j
public class RedisServiceTest {

	@Autowired
	private RedisService redisService;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("Redis set value with timeout test")
	void setValuesWithTimeoutTest() {
		// given
		final String key = "test_key";
		final String value = "test_value";

		// when
		redisService.setValuesWithTimeout(key, value, 3000);

		// then
		assertNotNull(redisService.getValues(key));

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertNull(redisService.getValues(key));
	}

	@Test
	@DisplayName("Redis list set and get")
	void redisListTest() throws JsonProcessingException {
		// given
		long roomId = 512312312312123L;
		long fromUserId = 1521431231L;
		long toUserId = 23423324l;
		String key = roomId + RedisConstants.ROOM_EMOTICON;
		RoomEmoticon value = RoomEmoticon.builder().roomId(roomId).emoticonCode(EmoticonCode.EMOTICON_TP1)
				.toUserId(toUserId).fromUserId(fromUserId).build();

		// when
		redisService.pushList(key, value);
		List<String> range = redisService.getList(key);
		RoomEmoticon roomEmoticon = objectMapper.readValue(range.get(0), RoomEmoticon.class);

		// then
		assertEquals(range.size(), 1);
		assertEquals(roomEmoticon.getRoomId(), roomId);
		assertEquals(roomEmoticon.getEmoticonCode(), EmoticonCode.EMOTICON_TP1);
		assertEquals(roomEmoticon.getFromUserId(), fromUserId);
		assertEquals(roomEmoticon.getToUserId(), toUserId);

		redisTemplate.delete(key);
	}

	@Test
	@DisplayName("set question Code and keyword Code")
	void setQuestionCode() throws JsonProcessingException{
		// given
		long userId = 123L;
		long roomId = 100L;
		List<Long> questionCode = Arrays.asList(5L, 10L, 15L);
		List<Long> keywordCode = Arrays.asList(1L, 2L, 3L);
		String key = roomId + "_" + userId + RedisConstants.QUESTION;
		KeywordSetDto keywordSetDto = KeywordSetDto.builder().userId(userId).roomId(roomId).keywordCode(keywordCode).questionCode(questionCode).build();

		// when
		redisService.pushQuestionList(roomId, userId, keywordSetDto);
		List<String> list = redisService.getList(key);
		KeywordSetDto keywordDtoValue = objectMapper.readValue(list.get(0), KeywordSetDto.class);

		// then
		assertEquals(keywordDtoValue.getQuestionCode().get(0), 5L);
		assertEquals(keywordDtoValue.getKeywordCode().get(0), 1L);

		redisTemplate.delete(key);
	}


}
