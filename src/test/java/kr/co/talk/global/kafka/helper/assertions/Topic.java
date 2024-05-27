package kr.co.talk.global.kafka.helper.assertions;

import lombok.Value;

@Value(staticConstructor = "topic")
public class Topic {
    String value;
}
