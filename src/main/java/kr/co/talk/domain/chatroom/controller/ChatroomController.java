package kr.co.talk.domain.chatroom.controller;

import kr.co.talk.global.client.UserClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import kr.co.talk.domain.chatroom.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatroomController {

    private final UserClient userClient;
    private final ChatRoomService chatRoomService;

    /**
     * 대화방 목록 조회 api
     * 
     * @param userId
     * @param teamCode
     * @return
     */
    @GetMapping("/find-chatrooms")
    public ResponseEntity<?> findChatRooms(@RequestHeader(value = "userId") String userId) {
        String teamCode = userClient.getTeamCode(Long.valueOf(userId));
        return ResponseEntity.ok(chatRoomService.findChatRooms(Long.valueOf(userId), teamCode));
    }

    /**
     * 닉네임 또는 이름으로 대화방 목록 조회 api
     * 
     * @param userId
     * @param teamCode
     * @param name
     * @return
     */
    @GetMapping("/find-chatrooms-with-name")
    public ResponseEntity<?> findChatRoomsWithName(@RequestHeader(value = "userId") String userId,
            String name) {
        String teamCode = userClient.getTeamCode(Long.valueOf(userId));
        return ResponseEntity
                .ok(chatRoomService.findChatRoomsByName(Long.valueOf(userId), teamCode, name));
    }

    /**
     * 대화방 생성 api
     * 
     * @param teamCode
     * @param userList
     * @return
     */
    @PostMapping("/create-chatroom")
    public ResponseEntity<?> createChatroom(@RequestHeader(value = "userId") String userId,
            @RequestBody List<Long> userList) {
        String teamCode = userClient.getTeamCode(Long.valueOf(userId));
        chatRoomService.createChatroom(teamCode, userList);
        return ResponseEntity.ok().build();
    }

}
