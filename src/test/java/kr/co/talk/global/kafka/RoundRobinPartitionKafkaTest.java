package kr.co.talk.global.kafka;

import kr.co.talk.global.kafka.helper.KafkaProducerTestHelper;
import kr.co.talk.global.kafka.helper.SimpleProducerCallback;
import kr.co.talk.global.kafka.helper.annotation.KafkaTest;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@KafkaTest(testDescriptions = "partition 의 수는 2개")
public class RoundRobinPartitionKafkaTest {

    KafkaProducer<String, String> producer;

    @BeforeEach
    void setUp() {
        producer = KafkaProducerTestHelper.getSimpleProducer(1);
    }

    @Test
    @DisplayName("Partition Key 를 입력하면 특정한 Partition 에 들어간다")
    void name() {
        for (int i = 0; i < 6; i++) {
            ProducerRecord<String, String> message = new ProducerRecord<>("my-topic2", "hello ~" + i);

            producer.send(message, SimpleProducerCallback.newOne());
        }

        producer.close();
    }

    /**
     * Partition 을 명시하지 않으면 Round Robin 으로 할당됨
     * topic: my-topic2, partition: 0, offset: 0, timestamp: 1716796155127
     * topic: my-topic2, partition: 0, offset: 1, timestamp: 1716796155127
     * topic: my-topic2, partition: 0, offset: 2, timestamp: 1716796155136
     * topic: my-topic2, partition: 0, offset: 3, timestamp: 1716796155136
     * topic: my-topic2, partition: 1, offset: 0, timestamp: 1716796155106
     * topic: my-topic2, partition: 1, offset: 1, timestamp: 1716796155127
     */
}
