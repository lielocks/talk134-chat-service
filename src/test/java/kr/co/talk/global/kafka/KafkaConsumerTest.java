package kr.co.talk.global.kafka;

import kr.co.talk.global.kafka.helper.assertions.KafkaAssertions;
import kr.co.talk.global.kafka.helper.KafkaConsumerTestHelper;
import kr.co.talk.global.kafka.helper.annotation.KafkaTest;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static kr.co.talk.global.kafka.helper.assertions.Topic.*;

@KafkaTest
public class KafkaConsumerTest {

    KafkaConsumer<String, String> consumer;

    @BeforeEach
    void setUp() {
        consumer = KafkaConsumerTestHelper.simpleConsumer();
    }

    @Test
    @DisplayName("topic 에 message 를 발행하면 consume 할 수 있다")
    void name() {
        KafkaConsumerTestHelper.produce("my-topic", "hello kafka!");

        consumer.subscribe(List.of("my-topic"));

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));

        KafkaAssertions.assertConsumedThat(records, topic("my-topic")).isEqualTo("hello kafka!");
    }
}
