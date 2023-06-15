package kr.co.talk.domain.chatroom.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class QuestionNotificationResponseDto {
    private User speaker;
    private List<User> userList;
    private String topic;

    @Builder
    @Data
    public static class User {
        private Long userId;
        private String profileUrl;
        private String nickname;
        private String userName;
    }

    @Builder
    @Data
    public static class Topic {
        private String keyword;

    }
}
