package kr.co.talk.domain.chatroom.model.event;

import java.util.List;
import kr.co.talk.domain.chatroom.dto.ChatroomNoticeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomNotiEventModel {

    private List<Long> userId;
    private ChatroomNoticeDto chatroomNoticeDto;
}
