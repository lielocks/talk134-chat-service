package kr.co.talk.global.kafka.helper.annotation;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@ContextConfiguration(classes = KafkaTest.KafkaTestConfiguration.class)
@EmbeddedKafka(partitions = 3, brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092"
})
public @interface TriplePartitionKafkaTest {
    String testDescriptions() default "";

}
