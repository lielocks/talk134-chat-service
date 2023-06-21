package kr.co.talk.domain.chatroom.repository;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import kr.co.talk.domain.chatroom.model.Chatroom;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class ChatroomRepositoryTest {

	@Autowired
	private ChatroomRepository chatroomRepository;
	
	@Test
	void findByTeamCodeAndNameTest() {
		// given
		String teamCode = "test_code2";
		List<Long> userIds = List.of(123456L);
		
		// when
		List<Chatroom> findByTeamCodeAndName = chatroomRepository.findByTeamCodeAndName(teamCode, userIds);
		log.info("findByTeamCodeAndName:::" + findByTeamCodeAndName);
	
		// then
		assertEquals(findByTeamCodeAndName.get(0).getTeamCode(), teamCode);
	}
	
	@Test
	void findByUsersInChatroomTest() {
	    // given
	    String roomId  = "48";
	    long userId = 82;
	    
	    List<Long> expectValue = List.of(82L, 79L, 62L, 48L);
	    
	    
	    // when
	    List<Long> userIds = chatroomRepository.findByUsersInChatroom(Long.valueOf(roomId), userId);
	    
	    // then
	    assertArrayEquals(expectValue.toArray(), userIds.toArray());
	}
}
