package kr.co.talk.domain.chatroom.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatEnterResponseDto {
    private List<Long> roomId;
    private List<ChatUserInfo> chatUserInfoList;
    @Data
    @AllArgsConstructor
    @Builder
    public static class ChatUserInfo {
        private Long userId;
        private String nickname;
        private String userName;
        private String profileUrl;
        private boolean activeFlag;
        private int socketFlag;
    }

}
