package kr.co.talk.domain.chatroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SocketFlagResponseDto {
    private int socketFlag;
    private List<TopicListDto> topicList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicListDto {
        private Long keywordId;
        private String keywordName;
        private Long questionId;
        private String questionName;
        private String depth;
    }
}
