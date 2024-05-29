package kr.co.talk.global.kafka;

import kr.co.talk.global.kafka.helper.annotation.TriplePartitionKafkaTest;
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
import static kr.co.talk.global.kafka.helper.KafkaConsumerTestHelper.simpleConsumer;

@TriplePartitionKafkaTest
public class Partition3toConsumer1Test {

    ExecutorService executorService = Executors.newFixedThreadPool(2);

    @AfterEach
    void tearDown() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    @Test
    @DisplayName("(Partition - 3) : (1 - Consumer) 라면 해당 인스턴스가 모든 partition 을 점유한다")
    void name() {

        produce("my-topic", "a", "b", "c", "A", "B", "C");

        KafkaConsumer<String, String> consumer = simpleConsumer();

        consumer.subscribe(List.of("my-topic")); // partition 구독

        executorService.submit(() -> {
            List<ConsumerRecord<String, String>> records = recordListFrom(consumer.poll(Duration.ofSeconds(2)));
            records.forEach(it ->
                    System.out.printf("partition:[%s], offset:[%s], value:[%s]\n", it.partition(), it.offset(), it.value()));
        });
    }
}
