package kr.co.talk.domain.chatroomusers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeywordSetDto {
    private Long roomId;
    private int registeredQuestionOrder;
    private List<Long> questionCode;
    private List<Long> keywordCode;
}
