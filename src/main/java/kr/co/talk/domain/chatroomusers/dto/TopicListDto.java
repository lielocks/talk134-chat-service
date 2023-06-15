package kr.co.talk.domain.chatroomusers.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicListDto {
    private String keyword;
    private Long questionId;
    private String questionName;
    private String depth;
}
