package kr.co.talk.global.kafka.helper.annotation;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@ContextConfiguration(classes = KafkaTest.KafkaTestConfiguration.class)
@EmbeddedKafka(partitions = 2, brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092"}
)
public @interface KafkaTest {
    String testDescriptions() default "";

    @Configuration
    class KafkaTestConfiguration {
        // 필요한 Kafka 설정만 포함
        @Bean
        public KafkaAdmin kafkaAdmin() {
            return new KafkaAdmin(Map.of("bootstrap.servers", "localhost:9092"));
        }

        @Bean
        public KafkaTemplate<String, String> kafkaTemplate() {
            return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(Map.of(
                    "bootstrap.servers", "localhost:9092",
                    "key.serializer", "org.apache.kafka.common.serialization.StringSerializer",
                    "value.serializer", "org.apache.kafka.common.serialization.StringSerializer"
            )));
        }
    }
}
