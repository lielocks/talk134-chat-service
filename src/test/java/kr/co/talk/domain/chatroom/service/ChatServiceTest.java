package kr.co.talk.domain.chatroom.service;

import fixture.ChatFixture;
import kr.co.talk.domain.chatroom.dto.ChatEnterDto;
import kr.co.talk.domain.chatroom.dto.ChatEnterResponseDto;
import kr.co.talk.domain.chatroom.dto.RequestDto;
import kr.co.talk.domain.chatroom.dto.RequestDto.ChatRoomEnterResponseDto;
import kr.co.talk.domain.chatroomusers.entity.ChatroomUsers;
import kr.co.talk.domain.chatroomusers.repository.ChatroomUsersRepository;
import kr.co.talk.global.client.UserClient;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@Slf4j
@ExtendWith(MockitoExtension.class)
@ActiveProfiles(profiles = "test")
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class ChatServiceTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private ChatroomUsersRepository chatroomUsersRepository;

    @MockBean
    UserClient userClient;

   ChatFixture chatFixture;

    @BeforeEach
    void createChatRoom() {
        chatFixture.setUpBeforeTest(userClient, chatRoomService);
    }

    @Test
    @DisplayName("채팅방 안에 사람들이 모두 참여했을때 다음 socket flag 로 set 됨")
    void socketFlagOneStatus() {
        // given
        List<ChatRoomEnterResponseDto> mockChatroomEnterResponseDto =
                chatFixture.createMockChatroomEnterResponseDto();

        // when
        doReturn(mockChatroomEnterResponseDto).when(userClient)
                .requiredEnterInfo(anyLong(), anyList());
        ChatEnterDto chatEnterDto1 = ChatEnterDto.builder().selected(true).socketFlag(0).userId(48L).roomId(1L).build();
        chatService.sendChatMessage(chatEnterDto1);

        ChatEnterDto chatEnterDto2 = ChatEnterDto.builder().selected(true).socketFlag(0).userId(53L).roomId(1L).build();
        ChatEnterResponseDto responseDto2 = chatService.sendChatMessage(chatEnterDto2);

        // then
        assertEquals(responseDto2.getChatroomUserInfos().get(0).getSocketFlag(), 0);
        assertEquals(responseDto2.getChatroomUserInfos().get(1).getSocketFlag(), 0);
        assertEquals(responseDto2.getChatroomUserInfos().get(2).getSocketFlag(), 0);

        // 마지막 사람이 message 를 보내고 난 후 변경되는 socket flag
        ChatEnterDto chatEnterDto3 = ChatEnterDto.builder().selected(true).socketFlag(0).userId(62L).roomId(1L).build();
        ChatEnterResponseDto responseDto3 = chatService.sendChatMessage(chatEnterDto3);

        assertEquals(responseDto3.getChatroomUserInfos().get(0).getSocketFlag(), 1);
        assertEquals(responseDto3.getChatroomUserInfos().get(1).getSocketFlag(), 1);
        assertEquals(responseDto3.getChatroomUserInfos().get(2).getSocketFlag(), 1);
    }
    
    @Test
    @Transactional
    @DisplayName("기존 채팅방에서 질문 선택까지 마친 후 (-> socket flag 4) 다른 채팅방에 참가할때 CHATROOM_USER_ALREADY_JOINED EXCEPTION")
    void chatRoomAlreadyJoined() {
        // given
        String teamCode = "abcdef";
        String chatroomName = "이솜2, 이담2, 해솔2";

        long createUserId = 48L;
        List<Long> userList = List.of(48L, 53L, 62L);

        RequestDto.FindChatroomResponseDto mockChatroomResponseDto = new RequestDto.FindChatroomResponseDto();
        mockChatroomResponseDto.setTeamCode(teamCode);
        mockChatroomResponseDto.setUserRole("ROLE_USER");

        RequestDto.CreateChatroomResponseDto mockCreateChatroomResponseDto = new RequestDto.CreateChatroomResponseDto();
        mockCreateChatroomResponseDto.setTimeout(10);
        mockCreateChatroomResponseDto.setTeamCode(teamCode);
        mockCreateChatroomResponseDto.setChatroomName(chatroomName);

        List<ChatRoomEnterResponseDto> mockChatroomEnterResponseDto =
                chatFixture.createMockChatroomEnterResponseDto();

        // when
        doReturn(mockChatroomResponseDto).when(userClient).findChatroomInfo(anyLong());
        doReturn(mockCreateChatroomResponseDto).when(userClient)
                .requiredCreateChatroomInfo(anyLong(), anyList());
        doReturn(mockChatroomEnterResponseDto).when(userClient)
                .requiredEnterInfo(anyLong(), anyList());

        // roomId 1L 로 redis room key 먼저 set 하여 이미 참여한 채팅방 기록 생성
        ChatEnterDto chatEnterDto = ChatEnterDto.builder().selected(true).socketFlag(0).userId(48L).roomId(1L).build();
        chatService.sendChatMessage(chatEnterDto);

        ChatroomUsers chatroomUsers = chatroomUsersRepository.findChatroomUsersByChatroomIdAndUserId(1L, 48L);
        chatroomUsers.setSocketFlag(4);
        chatroomUsers.setEntered(true);
        ChatroomUsers chatroomUsers2 = chatroomUsersRepository.findChatroomUsersByChatroomIdAndUserId(1L, 53L);
        chatroomUsers2.setSocketFlag(4);
        chatroomUsers2.setEntered(true);
        ChatroomUsers chatroomUsers3 = chatroomUsersRepository.findChatroomUsersByChatroomIdAndUserId(1L, 62L);
        chatroomUsers3.setSocketFlag(4);
        chatroomUsers3.setEntered(true);
        chatRoomService.createChatroom(createUserId, userList);

        // then
        ChatEnterDto chatEnterDto1 = ChatEnterDto.builder().selected(true).socketFlag(0).userId(48L).roomId(2L).build();
        CustomException customException = assertThrows(CustomException.class, () -> {
            chatService.sendChatMessage(chatEnterDto1);
        });

        assertEquals(customException.getCustomError(), CustomError.CHATROOM_USER_ALREADY_JOINED);
    }

}
