package kr.co.talk.global.kafka;

import kr.co.talk.global.kafka.helper.ConsumerRecordsHelper;
import kr.co.talk.global.kafka.helper.KafkaConsumerTestHelper;
import kr.co.talk.global.kafka.helper.annotation.TriplePartitionKafkaTest;
import lombok.extern.slf4j.Slf4j;
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

import static kr.co.talk.global.kafka.helper.ConsumerRecordsHelper.*;
import static kr.co.talk.global.kafka.helper.KafkaConsumerTestHelper.produce;
import static kr.co.talk.global.kafka.helper.KafkaConsumerTestHelper.simpleConsumer;

@Slf4j
@TriplePartitionKafkaTest(testDescriptions = "Consumer Group Test 를 위해 Partition 을 3개로 지정")
public class SameConsumerGroupTest {

    ExecutorService threads = Executors.newFixedThreadPool(2);

    @AfterEach
    void tearDown() throws InterruptedException {
        threads.shutdown();
        threads.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    @Test
    @DisplayName("하나의 Consumer Group 에 포함된 2개의 물리적 Consumer")
    void name() {

        produce("my-topic", "a", "b", "c", "A", "B", "C");

        KafkaConsumer<String, String> consumer1 = simpleConsumer("fancy-consumer-group");
        KafkaConsumer<String, String> consumer2 = simpleConsumer("not-fancy-consumer-group");
        consumer1.subscribe(List.of("my-topic"));
        consumer2.subscribe(List.of("my-topic"));

        threads.submit(() -> {
            List<ConsumerRecord<String, String>> records = recordListFrom(consumer1.poll(Duration.ofSeconds(2)));
            records.forEach(it ->
                    System.out.printf("consumer[%s] partition:[%s], offset:[%s], value:[%s]\n", "consumer1", it.partition(), it.offset(), it.value()));
        });

        threads.submit(() -> {
            List<ConsumerRecord<String, String>> records = recordListFrom(consumer2.poll(Duration.ofSeconds(2)));
            records.forEach(it ->
                    System.out.printf("consumer[%s] partition:[%s], offset:[%s], value:[%s]\n", "consumer2", it.partition(), it.offset(), it.value()));
        });
    }
}
