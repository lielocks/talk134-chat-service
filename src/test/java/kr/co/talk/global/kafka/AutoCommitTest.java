package kr.co.talk.global.kafka;

import kr.co.talk.global.kafka.helper.ConsumerRecordsHelper;
import kr.co.talk.global.kafka.helper.KafkaConsumerTestHelper;
import kr.co.talk.global.kafka.helper.annotation.SinglePartitionKafkaTest;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SinglePartitionKafkaTest
public class AutoCommitTest {

    KafkaConsumer<String, String> consumer;

    @BeforeEach
    void setUp() {
        consumer = KafkaConsumerTestHelper.consumerOf(Map.of(
                "max.poll.records", 3,
                "enable.auto.commit", "true"
        ));

        consumer.subscribe(List.of("my-topic"));
    }

    @Test
    @DisplayName("auto Commit 모드라서 poll() 이 호출될때 commit 된다")
    void name() {
        KafkaConsumerTestHelper.produce("my-topic", "a", "b", "c", "A", "B", "C");

        List<String> first = ConsumerRecordsHelper.messagesFrom(consumer.poll(Duration.ofSeconds(2)));
        assertThat(first).isEqualTo(List.of("a", "b", "c"));

        List<String> second = ConsumerRecordsHelper.messagesFrom(consumer.poll(Duration.ofSeconds(2)));
        assertThat(second).isEqualTo(List.of("A", "B", "C"));
    }
}
