package kr.co.talk.domain.chatroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatEnterResponseDto {
    private String nickname;
    private String userName;
    private boolean activeFlag;
}
