package kr.co.talk.domain.questionnotice.dto;

import kr.co.talk.domain.chatroom.dto.RequestDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class QuestionNoticeResponseDto {
    private QuestionNoticeMetadata metadata;
    private User speaker;
    private List<User> userList;
    private Topic topic;

    @Builder
    @Data
    public static class QuestionNoticeMetadata {
        private long senderId;
        private int questionNumber;
        private int finalQuestionNumber;
    }

    @Builder
    @Data
    public static class User {
        private Long userId;
        private String nickname;
        private String name;
        private String profileUrl;

        public static User of(RequestDto.ChatRoomEnterResponseDto enterResponseDto) {
            return User.builder()
                .userId(enterResponseDto.getUserId())
                .nickname(enterResponseDto.getNickname())
                .profileUrl(enterResponseDto.getProfileUrl())
                .name(enterResponseDto.getUserName())
                .build();
        }
    }

    @Builder
    @Data
    public static class Topic {
        private String keywordName;
        private Long questionId;
        private String questionName;
        private String depth;
        private List<String> questionGuide;
    }
}
