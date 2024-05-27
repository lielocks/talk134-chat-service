package kr.co.talk.global.kafka;

import kr.co.talk.global.kafka.helper.KafkaProducerTestHelper;
import kr.co.talk.global.kafka.helper.annotation.KafkaTest;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@KafkaTest
public class KafkaProducingTest {

    KafkaProducer<String, String> producer;

    @BeforeEach
    void setUp() {
        producer = KafkaProducerTestHelper.getSimpleProducer();
    }

    @Test
    @DisplayName("동기 전송, 메세지를 보내고 future 로 성공 실패 확인")
    void name() throws ExecutionException, InterruptedException {

        ProducerRecord<String, String> message = new ProducerRecord<>("my-topic", "hello~!");

        Future<RecordMetadata> future = producer.send(message);

        RecordMetadata metadata = future.get();

        assertThat(metadata.topic()).isEqualTo("my-topic");
    }
}
