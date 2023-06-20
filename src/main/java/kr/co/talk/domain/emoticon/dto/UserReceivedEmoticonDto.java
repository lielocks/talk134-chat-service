package kr.co.talk.domain.emoticon.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class UserReceivedEmoticonDto {
    private List<UserReceivedEmoticon> emoticonList;

    @Builder
    @Data
    public static class UserReceivedEmoticon {
        private int code;
        private int amount;
    }
}
