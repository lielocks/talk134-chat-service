//package kr.co.talk.global.config.redis;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.PreDestroy;
//import org.junit.jupiter.api.DisplayName;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//import redis.embedded.RedisServer;
//
//@DisplayName("Embedded Redis 설정")
//@Profile("test")
//@Configuration
//public class EmbeddedRedisConfig {
//    private final RedisServer redisServer;
//
//    public EmbeddedRedisConfig(@Value("${spring.redis.port}") int redisPort) {
//        this.redisServer = RedisServer.builder()
//                .port(redisPort)
//                .setting("maxmemory 10M").build();
//    }
//
//    @PostConstruct
//    public void startRedis() {
//        this.redisServer.start();
//    }
//
//    @PreDestroy
//    public void stopRedis() {
//        this.redisServer.stop();
//    }
//}