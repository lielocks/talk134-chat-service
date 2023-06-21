package kr.co.talk.domain.chatroom.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatEnterResponseDto {
    private SocketType type;
     private String checkInFlag;
     private Long requestId;
     private List<ChatroomUserInfo> chatroomUserInfos;
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class ChatroomUserInfo {
          private Long userId;
          private String nickname;
          private String userName;
          private String profileUrl;
          private boolean activeFlag;
          private int socketFlag;
     }
}
