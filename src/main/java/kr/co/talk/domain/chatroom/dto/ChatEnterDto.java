package kr.co.talk.domain.chatroom.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatEnterDto {
    private Long userId;
    private Long roomId;
    private boolean selected;
}
