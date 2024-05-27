package kr.co.talk.global.kafka;

import kr.co.talk.global.kafka.helper.KafkaProducerTestHelper;
import kr.co.talk.global.kafka.helper.annotation.KafkaTest;
import kr.co.talk.global.kafka.helper.SimpleProducerCallback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@KafkaTest
public class CheckCallbackProducerTest {

    KafkaProducer<String, String> producer;

    @BeforeEach
    void setUp() {
        producer = KafkaProducerTestHelper.getSimpleProducer();
    }

    @Test
    @DisplayName("Kafka Message 발행 후 Callback 을 실행시킬 수 있음")
    void testKafkaSendMessage() {
        ProducerRecord<String, String> message = new ProducerRecord<>("test-topic", "Hello~!");

        producer.send(message, SimpleProducerCallback.newOne());

        producer.close(); // 테스트 검증 위한 blocking
    }

}
