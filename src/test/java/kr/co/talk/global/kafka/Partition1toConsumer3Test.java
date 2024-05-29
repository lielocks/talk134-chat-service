package kr.co.talk.global.kafka;

import kr.co.talk.global.kafka.helper.KafkaConsumerTestHelper;
import kr.co.talk.global.kafka.helper.annotation.SinglePartitionKafkaTest;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static kr.co.talk.global.kafka.helper.ConsumerRecordsHelper.recordListFrom;
import static kr.co.talk.global.kafka.helper.KafkaConsumerTestHelper.produce;

@SinglePartitionKafkaTest(testDescriptions = "하나의 Partition 에 3개의 Consumer 가 붙을때")
public class Partition1toConsumer3Test {

    ExecutorService executorService = Executors.newFixedThreadPool(2);

    @AfterEach
    void tearDown() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    @Test
    @DisplayName("(Partition 1 : 3 Consumer 라면 오로지 하나의 Consumer 인스턴스만 consume 한다")
    void name() {
        produce("my-topic", "a", "b", "c", "A", "B", "C");

        KafkaConsumer<String, String> consumer1 = KafkaConsumerTestHelper.simpleConsumer();
        KafkaConsumer<String, String> consumer2 = KafkaConsumerTestHelper.simpleConsumer();
        KafkaConsumer<String, String> consumer3 = KafkaConsumerTestHelper.simpleConsumer();

        consumer1.subscribe(List.of("my-topic")); // partition 구독
        consumer2.subscribe(List.of("my-topic"));
        consumer3.subscribe(List.of("my-topic"));

        pollAndPrint(consumer1, "consumer1"); // consume o partition 은 하나만 점유됨
        pollAndPrint(consumer2, "consumer2"); // consume x
        pollAndPrint(consumer2, "consumer3"); // consume x

    }


    private void pollAndPrint(KafkaConsumer<String, String> consumer, String consumerName) {
        executorService.submit(() -> {
            List<ConsumerRecord<String, String>> records = recordListFrom(consumer.poll(Duration.ofSeconds(2)));
            records.forEach(it ->
                    System.out.printf("consumer[%s] partition:[%s], offset:[%s], value:[%s]\n", consumerName, it.partition(), it.offset(), it.value()));
        });
    }

}
