//package kr.co.talk.domain.chatroom.scheduler;
//
//import kr.co.talk.domain.chatroom.service.ChatRoomSender;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import lombok.extern.slf4j.Slf4j;
//
//import org.springframework.kafka.test.context.EmbeddedKafka;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.util.List;
//
//@Slf4j
//@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
//@ActiveProfiles(profiles = "test")
//@SpringBootTest
//public class ChatroomSenderTest {
//
//    @Autowired
//    private ChatRoomSender chatRoomSender;
//
//    @Test
//    public void testSendEndChatting() throws Exception {
//        long roomId = 123L;
//        long userId = 456L;
//
//        chatRoomSender.sendEndChatting(roomId, userId);
//
//        // 메시지가 잘 전송되었는지 확인
//        List<String> event = chatRoomSender.getEndChattingEvent();
//        System.out.println("event :: " + event);
//
//    }
//
//}
