package kr.co.talk.domain.keyword.service;

import kr.co.talk.domain.chatroom.dto.RequestDto;
import kr.co.talk.domain.chatroom.dto.SocketFlagResponseDto;
import kr.co.talk.domain.chatroom.service.ChatRoomService;
import kr.co.talk.domain.chatroomusers.dto.KeywordSendDto;
import kr.co.talk.domain.chatroomusers.repository.KeywordRepository;
import kr.co.talk.domain.chatroomusers.repository.QuestionRepository;
import kr.co.talk.domain.chatroomusers.service.KeywordService;
import kr.co.talk.global.client.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@Slf4j
@ExtendWith(MockitoExtension.class)
public class KeywordServiceTest {

    @Autowired
    private KeywordService keywordService;

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ChatRoomService chatRoomService;

    @MockBean
    UserClient userClient;

    @BeforeEach
    void createChatRoom() {
        String teamCode = "abcdef";
        String chatroomName = "이솜, 이담, 해솔";

        long createUserId = 48L;
        List<Long> userList = List.of(48L, 53L, 62L);

        RequestDto.FindChatroomResponseDto mockChatroomResponseDto = new RequestDto.FindChatroomResponseDto();
        mockChatroomResponseDto.setTeamCode(teamCode);
        mockChatroomResponseDto.setUserRole("ROLE_USER");

        RequestDto.CreateChatroomResponseDto mockCreateChatroomResponseDto = new RequestDto.CreateChatroomResponseDto();
        mockCreateChatroomResponseDto.setTimeout(10);
        mockCreateChatroomResponseDto.setTeamCode(teamCode);
        mockCreateChatroomResponseDto.setChatroomName(chatroomName);

        doReturn(mockChatroomResponseDto).when(userClient).findChatroomInfo(anyLong());
        doReturn(mockCreateChatroomResponseDto).when(userClient)
                .requiredCreateChatroomInfo(anyLong(), anyList());

        chatRoomService.createChatroom(createUserId, userList);
    }


    @Test
    @DisplayName("24시간 내로 채팅방 참여 기록 있을 시 nickname map 의 1st recommendation 과 상관 없이 random question list 로 set")
    void withinOneDayRandomQuestion() {
        KeywordSendDto keywordSendDto = KeywordSendDto.builder().keywordCode(List.of(1L, 2L, 3L)).roomId(1L).userId(48L).build();
        doReturn("co-a-sp").when(userClient).getUserImgCode(48L);
        SocketFlagResponseDto questionWithFlag = keywordService.setQuestionWithFlag(keywordSendDto);
        log.info("questionWithFlag {}", questionWithFlag.getTopicList());
    }

}
