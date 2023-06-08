package kr.co.talk.domain.chatroom.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedbackOptionalDto {
    private long userId;
    private long roomId;
    private String sentence;
    private int score;
    private List<Feedback> feedback;

    // @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    private static class Feedback {
        private long toUserId;
        private String review;
        private int feedbackScore;
    }
}
