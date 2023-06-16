package kr.co.talk.domain.chatroomusers.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicListDto {
    private Long keywordId;
    private String keywordName;
    private Long questionId;
    private String questionName;
    private String depth;
}
