package kr.co.talk.global.kafka;

import kr.co.talk.global.kafka.helper.annotation.KafkaTest;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static kr.co.talk.global.kafka.helper.ConsumerRecordsHelper.messagesFrom;
import static kr.co.talk.global.kafka.helper.KafkaConsumerTestHelper.produce;
import static kr.co.talk.global.kafka.helper.KafkaConsumerTestHelper.simpleConsumer;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@KafkaTest(testDescriptions = "Partition 수는 2개")
public class DiffConsumerGroupTest {

    ExecutorService executorService = Executors.newFixedThreadPool(2);

    private KafkaConsumer<String, String> consumer1;
    private KafkaConsumer<String, String> consumer2;

    @AfterEach
    void tearDown() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        consumer1 = simpleConsumer("group.id", "g1");
        consumer2 = simpleConsumer("group.id", "g2");

        consumer1.subscribe(List.of("my-topic")); // partition 구독
        consumer2.subscribe(List.of("my-topic"));
    }

    @Test
    @DisplayName("컨슈머 그룹 id 가 다르면 동일한 메시지에 대해서 각각의 컨슈머가 소비한다")
    void name() {
        produce("my-topic",
                "a", "b", "c",
                "1", "2", "3");

        // Consumer Group 이 다르므로 모든 메시지 소비
        executorService.submit(() -> {
            List<String> messages = messagesFrom(consumer1.poll(Duration.ofSeconds(2)));
            assertThat(messages).isEqualTo(List.of("a", "b", "c", "1", "2", "3"));
        });

        // Consumer Group 이 다르므로 모든 메시지 소비
        executorService.submit(() -> {
            List<String> messages = messagesFrom(consumer2.poll(Duration.ofSeconds(2)));
            assertThat(messages).isEqualTo(List.of("a", "b", "c", "1", "2", "3"));
        });
    }
}
