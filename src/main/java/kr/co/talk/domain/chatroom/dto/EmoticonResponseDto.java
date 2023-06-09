package kr.co.talk.domain.chatroom.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class EmoticonResponseDto {
    private int emoticonCode;
}
