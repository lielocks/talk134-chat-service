package fixture;

import kr.co.talk.domain.chatroom.dto.RequestDto;
import kr.co.talk.domain.chatroom.service.ChatRoomService;
import kr.co.talk.global.client.UserClient;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;

@Data
public class ChatFixture {

    private ChatRoomService chatRoomService;
    private UserClient userClient;

    @Transactional
    public void setUpBeforeTest(UserClient userClient, ChatRoomService chatRoomService) {
        RequestDto.FindChatroomResponseDto mockChatroomResponseDto = new RequestDto.FindChatroomResponseDto();
        mockChatroomResponseDto.setTeamCode("abcdef");
        mockChatroomResponseDto.setUserRole("ROLE_USER");

        RequestDto.CreateChatroomResponseDto mockCreateChatroomResponseDto = new RequestDto.CreateChatroomResponseDto();
        mockCreateChatroomResponseDto.setTimeout(10);
        mockCreateChatroomResponseDto.setTeamCode("abcdef");
        mockCreateChatroomResponseDto.setChatroomName("이솜, 이담, 해솔");

        doReturn(mockChatroomResponseDto).when(userClient).findChatroomInfo(anyLong());
        doReturn(mockCreateChatroomResponseDto).when(userClient)
                .requiredCreateChatroomInfo(anyLong(), anyList());

        chatRoomService.createChatroom(48L, List.of(48L, 53L, 62L));
    }

    public List<RequestDto.ChatRoomEnterResponseDto> createMockChatroomEnterResponseDto() {
        List<RequestDto.ChatRoomEnterResponseDto> mockChatroomEnterResponseDto = new ArrayList<>();
        RequestDto.ChatRoomEnterResponseDto enterResponse1 =
                RequestDto.ChatRoomEnterResponseDto.builder().nickname("차가운 매의 낮잠").userName("이솜").userId(48L).profileUrl("https://134-back.s3.ap-northeast-2.amazonaws.com/profile/co-a-sp.png").build();
        mockChatroomEnterResponseDto.add(enterResponse1);

        RequestDto.ChatRoomEnterResponseDto enterResponse2 =
                RequestDto.ChatRoomEnterResponseDto.builder().nickname("차가운 바람의 일격").userName("이담").userId(53L).profileUrl("https://134-back.s3.ap-northeast-2.amazonaws.com/profile/co-d-bl.png").build();
        mockChatroomEnterResponseDto.add(enterResponse2);

        RequestDto.ChatRoomEnterResponseDto enterResponse3 =
                RequestDto.ChatRoomEnterResponseDto.builder().nickname("떠오르는 바람의 일격").userName("해솔").userId(62L).profileUrl("https://134-back.s3.ap-northeast-2.amazonaws.com/profile/fl-d-bl.png").build();
        mockChatroomEnterResponseDto.add(enterResponse3);

        return mockChatroomEnterResponseDto;
    }
}
