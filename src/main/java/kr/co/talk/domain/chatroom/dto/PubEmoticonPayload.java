package kr.co.talk.domain.chatroom.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PubEmoticonPayload {
    private Long userId;
    private Long roomId;
    private Long toUserId;
    private int emoticonCode;
}
