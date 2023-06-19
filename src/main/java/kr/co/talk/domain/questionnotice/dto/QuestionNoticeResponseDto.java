package kr.co.talk.domain.questionnotice.dto;

import kr.co.talk.domain.chatroom.dto.RequestDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class QuestionNoticeResponseDto {
    private RequestDto.ChatRoomEnterResponseDto speaker;
    private List<RequestDto.ChatRoomEnterResponseDto> userList;
    private Topic topic;
    private int questionCount;
    private boolean endFlag;

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
