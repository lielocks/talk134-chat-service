package kr.co.talk.domain.chatroom.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequestDto {
    /**
     * uesr-service
     */
    @Data
    public static class NameResponseDto {
        private String name;
    }

    /**
     * uesr-service
     */
    @Data
    public static class TeamCodeResponseDto {
        private String teamCode;
    }

    /**
     * uesr-service
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

    @Data
    public static class ChatRoomEnterResponseDto {
        private String nickname;
        private String userName;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class UserStatusDto {
        private boolean isToday; // 오늘 status update 된적있는지
        private String name;
        private String nickname;
        private int statusEnergy;
        private int statusRelation;
        private int statusStress;
        private int statusStable;
    }
}
