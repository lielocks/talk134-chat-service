package kr.co.talk.domain.questionnotice.dto;

import kr.co.talk.domain.chatroom.dto.RequestDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class QuestionNoticeResponseDto {
    private QuestionNoticeMetadata metadata;
    private RequestDto.ChatRoomEnterResponseDto speaker;
    private List<RequestDto.ChatRoomEnterResponseDto> userList;
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
    public static class Topic {
        private String keyword;
        private Long questionCode;
        private String questionName;
        private String depth;
        private List<String> questionGuide;
    }
}
