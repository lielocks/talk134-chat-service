package kr.co.talk.global.kafka;

import kr.co.talk.global.kafka.helper.KafkaConsumerTestHelper;
import kr.co.talk.global.kafka.helper.annotation.KafkaTest;
import kr.co.talk.global.kafka.helper.assertions.Topic;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static kr.co.talk.global.kafka.helper.assertions.KafkaAssertions.assertConsumedThat;
import static kr.co.talk.global.kafka.helper.KafkaConsumerTestHelper.produce;

@KafkaTest
public class MultiTopicConsumeTest {

    KafkaConsumer<String, String> consumer;

    @BeforeEach
    void setUp() {
        consumer = KafkaConsumerTestHelper.simpleConsumer();
    }

    @Test
    @DisplayName("Kafka 의 consumer 는 두개 혹은 그 이상의 Topic 을 consume 할 수 있다")
    void name() {
        produce("my-topic-1", "Hello Kafka");
        produce("my-topic-2", "Bye Kafka");

        consumer.subscribe(List.of("my-topic-1", "my-topic-2"));

        ConsumerRecords<String, String> actual = consumer.poll(Duration.ofSeconds(10));

        assertConsumedThat(actual, Topic.topic("my-topic-1")).isEqualTo("Hello Kafka");
        assertConsumedThat(actual, Topic.topic("my-topic-2")).isEqualTo("Bye Kafka");
    }
}
