package kr.co.talk.domain.chatroom.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ChatEnterResponseDto extends SocketResponseDto{
     private String checkInFlag;
     private Long requestId;
     private List<ChatroomUserInfo> chatroomUserInfos;
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class ChatroomUserInfo {
          private Long userId;
          private String nickname;
          private String name;
          private String profileUrl;
          private boolean activeFlag;
          private int socketFlag;
     }
}
