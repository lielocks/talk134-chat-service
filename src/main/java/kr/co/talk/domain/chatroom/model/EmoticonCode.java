package kr.co.talk.domain.chatroom.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum EmoticonCode {
    EMOTICON_TP1(1), 
    EMOTICON_TP2(2), 
    EMOTICON_TP3(3), 
    EMOTICON_TP4(4), 
    EMOTICON_TP5(5), 
    EMOTICON_TP6(6);

    private int code;

    public static EmoticonCode of(int code) throws IllegalArgumentException {
        return Stream.of(EmoticonCode.values()).filter(e -> e.getCode() == code).findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
