package kr.co.talk.domain.chatroom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatEnterDto {
    private Long userId;
    private Long roomId;
    private boolean selected;
    private int socketFlag;
}
