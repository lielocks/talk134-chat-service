package kr.co.talk.domain.timeoutnotice;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TimeoutNoticeDto {
    private boolean fiveMinuteLeft;
}
