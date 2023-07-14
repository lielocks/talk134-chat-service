package kr.co.talk.global.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import kr.co.talk.global.service.redis.RedisService;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class EmbeddedRedisTest {

    @Autowired
    RedisService redisService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    @DisplayName("embedded redis test")
    void embeddedRedisTest() {
        // given
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
        String key = "testkey";
        String value = "testvalue";

        // when
        // redis transaction
        redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public <K, V> List<Object> execute(RedisOperations<K, V> operations) {
                redisTemplate.multi();
                opsForValue.set(key, value);
                return redisTemplate.exec();
            }
        });


        // then
        assertEquals(value, opsForValue.get(key));
    }
}
