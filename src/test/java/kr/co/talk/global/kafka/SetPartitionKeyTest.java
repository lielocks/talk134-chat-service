package kr.co.talk.global.kafka;

import kr.co.talk.global.kafka.helper.KafkaProducerTestHelper;
import kr.co.talk.global.kafka.helper.SimpleProducerCallback;
import kr.co.talk.global.kafka.helper.annotation.KafkaTest;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@KafkaTest(testDescriptions = "Partition 의 수는 2개")
public class SetPartitionKeyTest {

    KafkaProducer<String, String> producer;

    @BeforeEach
    void setUp() {
        producer = KafkaProducerTestHelper.getSimpleProducer(1);
    }

    @Test
    @DisplayName("Partition key 를 입력하면 특정한 partition 에 들어간다")
    void name() {
        for (int i = 0; i < 6; i++) {
            int partitionKey = isEven(i) ? 0 : 1;
            String key = isEven(i) ? "key-0" : "key-1";
            ProducerRecord<String, String> message = new ProducerRecord<>("my-topic2", partitionKey, key,"hello ~ " + i);

            producer.send(message, SimpleProducerCallback.newOne());
        }

        producer.close();
    }

    /**
     * 균등하게 partition 에 들어감
     * topic: my-topic2, partition: 1, offset: 0, timestamp: 1716795495842
     * topic: my-topic2, partition: 0, offset: 0, timestamp: 1716795495823
     * topic: my-topic2, partition: 0, offset: 1, timestamp: 1716795495842
     * topic: my-topic2, partition: 1, offset: 1, timestamp: 1716795495842
     * topic: my-topic2, partition: 1, offset: 2, timestamp: 1716795495850
     * topic: my-topic2, partition: 0, offset: 2, timestamp: 1716795495850
     */

    private static boolean isEven(int i) {
        return i % 2 == 0;
    }
}
