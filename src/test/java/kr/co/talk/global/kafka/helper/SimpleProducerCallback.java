package kr.co.talk.global.kafka.helper;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

import static java.util.Objects.nonNull;

@Slf4j
public class SimpleProducerCallback implements Callback {

    public static SimpleProducerCallback newOne() {
        return new SimpleProducerCallback();
    }

    @Override
    public void onCompletion(RecordMetadata metadata, Exception e) {
        if (nonNull(metadata)) {
            log.info("Message Sent Successfully, topic: {}, partition: {}, offset: {}, timestamp: {}",
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset(),
                    metadata.timestamp());
        } else {
            log.error("Message Send Failed", e);
        }
    }
}
