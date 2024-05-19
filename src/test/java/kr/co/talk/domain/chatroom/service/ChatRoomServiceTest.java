package kr.co.talk.domain.chatroom.service;

import java.util.List;
import java.util.stream.Collectors;

import kr.co.talk.domain.chatroomusers.repository.ChatroomUsersRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import kr.co.talk.domain.chatroom.dto.RequestDto.CreateChatroomResponseDto;
import kr.co.talk.domain.chatroom.dto.RequestDto.FindChatroomResponseDto;
import kr.co.talk.domain.chatroom.model.Chatroom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import kr.co.talk.domain.chatroomusers.entity.ChatroomUsers;
import kr.co.talk.global.client.UserClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest // 트랜잭션 제거
@ActiveProfiles(profiles = "test")
public class ChatRoomServiceTest {
    @Autowired
    private ChatroomUsersRepository chatroomUsersRepository;

    @MockBean
    UserClient userClient;

    @AfterEach
    void clearUp() {
        chatroomUsersRepository.deleteAll();
    }
    
    @Test
    @Order(1)
    @DisplayName("채팅방 잘 만들어지는지 확인")
    public void createChatroom() {
        // given
        String teamCode = "teamCode_test";
        String chatroomName = "석홍, 용현, 해솔";

        List<Long> userList = List.of(1L, 2L, 3L);

        FindChatroomResponseDto mockChatroomResponseDto = new FindChatroomResponseDto();
        mockChatroomResponseDto.setTeamCode(teamCode);
        mockChatroomResponseDto.setUserRole("ROLE_USER");

        CreateChatroomResponseDto mockCreateChatroomResponseDto = new CreateChatroomResponseDto();
        mockCreateChatroomResponseDto.setTimeout(10);
        mockCreateChatroomResponseDto.setTeamCode(teamCode);
        mockCreateChatroomResponseDto.setChatroomName(chatroomName);


        // when
        doReturn(mockChatroomResponseDto).when(userClient).findChatroomInfo(anyLong());
        doReturn(mockCreateChatroomResponseDto).when(userClient)
                .requiredCreateChatroomInfo(anyLong(), anyList());

        List<ChatroomUsers> chatroomUsers = userList.stream().map(userId -> {
            return ChatroomUsers.builder()
                    .chatroom(Chatroom.builder()
                            .name(mockCreateChatroomResponseDto.getChatroomName())
                            .teamCode(mockCreateChatroomResponseDto.getTeamCode())
                            .build())
                    .userId(userId)
                    .build();
        }).collect(Collectors.toList());

        chatroomUsersRepository.saveAll(chatroomUsers);
        long resultCount = chatroomUsersRepository.count();

        assertEquals(resultCount, 3);
    }

    @Test
    @Order(2)
    @DisplayName("rollback 검증")
    void rollback() {
        long afterCount = chatroomUsersRepository.count();
        assertEquals(afterCount, 0);
    }

    // @Test
    // @Transactional
    // @Rollback(false)
    // @DisplayName("대화방 목록 조회 api")
    // public void findChatRoomsTest() {
    // // given
    // long userId = 2L;
    // String teamCode = "teamCode_test";
    //
    // // redis에 이모티콘 저장
    // RoomEmoticon roomEmoticon1 =
    // RoomEmoticon.builder().emoticonCode(EmoticonCode.EMOTICON_TP1).fromUserId(user1)
    // .toUserId(user2).roomId(1L).build();
    //
    // RoomEmoticon roomEmoticon2 =
    // RoomEmoticon.builder().emoticonCode(EmoticonCode.EMOTICON_TP1).fromUserId(user1)
    // .toUserId(user3).roomId(1L).build();
    //
    // RoomEmoticon roomEmoticon3 =
    // RoomEmoticon.builder().emoticonCode(EmoticonCode.EMOTICON_TP2).fromUserId(user2)
    // .toUserId(user3).roomId(1L).build();
    //
    // RoomEmoticon roomEmoticon4 =
    // RoomEmoticon.builder().emoticonCode(EmoticonCode.EMOTICON_TP2).fromUserId(user1)
    // .toUserId(user1).roomId(3424L).build();
    //
    // RoomEmoticon roomEmoticon5 =
    // RoomEmoticon.builder().emoticonCode(EmoticonCode.EMOTICON_TP4).fromUserId(user2)
    // .toUserId(user3).roomId(1L).build();
    //
    // redisService.pushList(1L + RedisConstants.ROOM_EMOTICON, roomEmoticon1);
    // redisService.pushList(1L + RedisConstants.ROOM_EMOTICON, roomEmoticon2);
    // redisService.pushList(1L + RedisConstants.ROOM_EMOTICON, roomEmoticon3);
    // redisService.pushList(1L + RedisConstants.ROOM_EMOTICON, roomEmoticon4);
    // redisService.pushList(1L + RedisConstants.ROOM_EMOTICON, roomEmoticon5);
    //
    // // when
    // // List<ChatroomListDto> findChatRooms = chatRoomService.findChatRooms(userId, teamCode);
    //
    // // log.info("findChatRooms::::::" + findChatRooms);
    // // // then
    // // assertEquals(findChatRooms.get(0).getEmoticons().size(), 3);
    // // assertTrue(findChatRooms.get(0).isJoinFlag());
    // }

}
