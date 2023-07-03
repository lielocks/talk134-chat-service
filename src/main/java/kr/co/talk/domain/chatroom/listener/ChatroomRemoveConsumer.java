package kr.co.talk.domain.chatroom.listener;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import kr.co.talk.domain.chatroom.model.Chatroom;
import kr.co.talk.domain.chatroom.repository.ChatroomRepository;
import kr.co.talk.domain.chatroomusers.entity.ChatroomUsers;
import kr.co.talk.domain.questionnotice.service.QuestionNoticeRedisService;
import kr.co.talk.global.constants.KafkaConstants;
import kr.co.talk.global.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatroomRemoveConsumer {
	private final ChatroomRepository chatroomRepository;
	private final QuestionNoticeRedisService questionNoticeRedisService;
	private final RedisService redisService;

	/**
	 * 
	 * @param msg
	 * @param ack
	 * @throws JsonProcessingException
	 */
	@KafkaListener(topics = KafkaConstants.TOPIC_REMOVE_CHATTING, groupId = "${spring.kafka.group}", containerFactory = "concurrentKafkaListenerContainerFactory")
	public void removeChatting(String msg, Acknowledgment ack) throws JsonProcessingException {
		long roomId = Long.valueOf(msg);
		log.info("Received Msg Chat server, message : {}", roomId);

		// 채팅방 종료 후 채팅방 remove
		Optional<Chatroom> chatroom = chatroomRepository.findById(roomId);
		// 질문 알림 조회용으로 redis에 저장했던 데이터도 삭제.
		chatroom.ifPresent(c -> {
			chatroomRepository.delete(c);
			questionNoticeRedisService.deleteQuestionNumber(c.getChatroomId());
			questionNoticeRedisService.deleteQuestionManagementDto(c.getChatroomId());

			List<Long> userIds = c.getChatroomUsers().stream().map(ChatroomUsers::getChatroomUserId)
					.collect(Collectors.toList());

			for(long userId: userIds) {
				redisService.deleteQuestionKey(String.valueOf(roomId), userId);
			}
		});

		redisService.deleteCountAndChatroomKey(roomId);

		// kafka commit
		ack.acknowledge();
	}
}
