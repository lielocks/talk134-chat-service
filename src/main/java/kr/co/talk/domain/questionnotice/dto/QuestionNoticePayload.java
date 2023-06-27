package kr.co.talk.domain.questionnotice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionNoticePayload {
    private long userId;
    private int questionNumber;
}
