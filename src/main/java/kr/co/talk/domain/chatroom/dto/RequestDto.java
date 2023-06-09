package kr.co.talk.domain.chatroom.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequestDto {
    /**
     * user-service
     */
    @Data
    public static class NameResponseDto {
        private String name;
    }

    /**
     * user-service
     */
    @Data
    public static class TeamCodeResponseDto {
        private String teamCode;
    }

    /**
     * user-service
     */
    @Data
    public static class UserIdResponseDto {
        private Long userId;
        private String userName;
    }

    /**
     * user-service로 요청할 chatroom create시 필요한 dto
     */
    @Data
    public static class CreateChatroomResponseDto {
        private int timeout;
        private String teamCode;
        private String chatroomName;
    }

    /**
     * 대화방 대기화면에 나타날 user info dto
     */
    @Data
    public static class ChatRoomEnterResponseDto {
        private Long userId;
        private String nickname;
        private String userName;
        private String profileUrl;
    }
}
