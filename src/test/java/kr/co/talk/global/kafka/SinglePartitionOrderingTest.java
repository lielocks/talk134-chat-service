package kr.co.talk.global.kafka;

import kr.co.talk.global.kafka.helper.KafkaConsumerTestHelper;
import kr.co.talk.global.kafka.helper.annotation.SinglePartitionKafkaTest;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static kr.co.talk.global.kafka.helper.ConsumerRecordsHelper.messagesFrom;
import static kr.co.talk.global.kafka.helper.KafkaConsumerTestHelper.produce;
import static org.assertj.core.api.Assertions.*;

@SinglePartitionKafkaTest(testDescriptions = "Topic 에 Partition 이 하나인 Test")
public class SinglePartitionOrderingTest {

    KafkaConsumer<String, String> consumer;

    @BeforeEach
    void setUp() {
        consumer = KafkaConsumerTestHelper.simpleConsumer();
    }

    @Test
    @DisplayName("partition 이 하나라서 발행한 순서대로 consume 된다")
    void name() {
        produce("my-topic", "a", "b", "c");
        produce("my-topic", "A", "B", "C");

        consumer.subscribe(List.of("my-topic"));

        ConsumerRecords<String, String> actual = consumer.poll(Duration.ofSeconds(2));

        List<String> messages = messagesFrom(actual);

        // 발행 순서대로 consume 된다
        assertThat(messages)
                .isEqualTo(
                        List.of("a", "b", "c", "A", "B", "C")
                );
    }
}
