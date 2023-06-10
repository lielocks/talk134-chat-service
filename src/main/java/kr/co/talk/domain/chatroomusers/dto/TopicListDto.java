package kr.co.talk.domain.chatroomusers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicListDto {
    private String keyword;
    private String questionName;
    private String depth;
}
