package kr.co.talk.domain.chatroom.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum EmoticonCode {
    EMOTICON_TP1(1, "Love"), 
    EMOTICON_TP2(2, "Like"), 
    EMOTICON_TP3(3, "Hug"), 
    EMOTICON_TP4(4, "Sad"), 
    EMOTICON_TP5(5, "You're Right"), 
    EMOTICON_TP6(6, "Angry");

    private int code;
    private String name;

    public static EmoticonCode of(int code) throws IllegalArgumentException {
        return Stream.of(EmoticonCode.values()).filter(e -> e.getCode() == code).findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
