package kr.co.talk.domain.chatroom.scheduler;

import kr.co.talk.domain.chatroom.dto.ChatroomNoticeDto;
import kr.co.talk.domain.timeoutnotice.TimeoutNoticeService;
import kr.co.talk.global.constants.RedisConstants;
import kr.co.talk.global.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

/**
 * chatroom의 timeout을 걸고, 대화 마감을 해주기 위한 scheduler
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatroomTimeoutScheduler {
	private final RedisService redisService;
	private final TimeoutNoticeService timeoutNoticeService;

	private static final long NOTICE_5MINUTE = Duration.ofMinutes(5).toMillis();

	@Scheduled(fixedRate = 3000)
	public void scheduleNoticeTask() {
		// 채팅방 timeout check
		Map<String, Object> chatroomNoticeEntry = redisService.getEntry(RedisConstants.ROOM_NOTICE,
				ChatroomNoticeDto.class);

		chatroomNoticeEntry.forEach((roomId, value) -> {
			ChatroomNoticeDto cn = (ChatroomNoticeDto) value;
			if (cn.isActive()) {
				long currentTime = System.currentTimeMillis();

				// 5분 전 알림
				if (cn.getCreateTime() + cn.getTimeout() > currentTime
						&& cn.getCreateTime() + cn.getTimeout() <= currentTime + NOTICE_5MINUTE && !cn.isNotice()) {
					cn.setNotice(true); // 5분전 공지 flag
					redisService.pushMap(RedisConstants.ROOM_NOTICE, roomId, cn);
					timeoutNoticeService.sendFiveMinuteLeftMessage(cn.getRoomId());
					// 대화 마감 알림
				} else if (cn.getCreateTime() + cn.getTimeout() <= currentTime) {
					redisService.deleteMap(RedisConstants.ROOM_NOTICE, roomId);
					timeoutNoticeService.sendTimedOutMessage(cn.getRoomId());
				}
			}

		});

	}

}
