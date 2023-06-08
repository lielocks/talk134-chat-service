package kr.co.talk.domain.chatroom.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import kr.co.talk.domain.chatroom.dto.FeedbackOptionalDto;
import kr.co.talk.domain.chatroom.dto.RequestDto.CreateChatroomResponseDto;
import kr.co.talk.domain.chatroom.dto.RequestDto.UserIdResponseDto;
import kr.co.talk.domain.chatroom.service.ChatRoomService;
import kr.co.talk.global.client.UserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    public ResponseEntity<?> findChatRooms(@RequestHeader(value = "userId") Long userId) {
        String teamCode = userClient.getTeamCode(userId).getTeamCode();
        return ResponseEntity.ok(chatRoomService.findChatRooms(userId, teamCode));
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
    public ResponseEntity<?> findChatRoomsWithName(@RequestHeader(value = "userId") Long userId,
            String name) {
        String teamCode = userClient.getTeamCode(userId).getTeamCode();
        List<UserIdResponseDto> userIdResponseDtos = userClient.getUserIdByName(name);
        return ResponseEntity
                .ok(chatRoomService.findChatRoomsByName(userId, teamCode, userIdResponseDtos));
    }

    /**
     * 대화방 생성 api
     * 
     * @param teamCode
     * @param userList
     * @return
     */
    @PostMapping("/create-chatroom")
    public ResponseEntity<?> createChatroom(@RequestHeader(value = "userId") Long userId,
            @RequestBody List<Long> userList) {
        chatRoomService.createChatroom(userId, userList);
        return ResponseEntity.ok().build();
    }

    /**
     * 피드백 선택형 등록 api
     */
    @PostMapping("/feedback/create/optional")
    public void feedbackCreateOptional(@RequestBody FeedbackOptionalDto feedbackOptionalDto) {
        log.info("feedbackOptionalDto::::" + feedbackOptionalDto);
    }

}
