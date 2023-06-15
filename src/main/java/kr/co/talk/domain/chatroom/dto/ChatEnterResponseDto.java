package kr.co.talk.domain.chatroom.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatEnterResponseDto {
     private Long userId;
     private String nickname;
     private String userName;
     private String profileUrl;
     private boolean activeFlag;
     private int socketFlag;
}
