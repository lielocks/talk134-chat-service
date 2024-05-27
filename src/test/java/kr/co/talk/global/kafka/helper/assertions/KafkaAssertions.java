package kr.co.talk.global.kafka.helper.assertions;

import kr.co.talk.global.kafka.helper.assertions.Partition;
import kr.co.talk.global.kafka.helper.assertions.Topic;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.assertj.core.api.AbstractStringAssert;

import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.*;

public class KafkaAssertions {

    public static AbstractStringAssert<?> assertConsumedThat(ConsumerRecords<String, String> record, Topic topic) {
        String value = StreamSupport.stream(record.spliterator(), false)
                .filter(i -> i.topic().equals(topic.getValue()))
                .findFirst()
                .orElseGet(null)
                .value();

        return assertThat(value);
    }

    public static AbstractStringAssert<?> assertConsumedThat(ConsumerRecords<String, String> record, Partition partition) {
        String value = StreamSupport.stream(record.spliterator(), false)
                .filter(i -> i.partition() == partition.getValue())
                .findFirst()
                .orElseGet(null)
                .value();

        return assertThat(value);
    }
}
