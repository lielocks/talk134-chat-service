package kr.co.talk.global.kafka.helper.assertions;

import lombok.Value;

@Value(staticConstructor = "partition")
public class Partition {
    int value;
}
