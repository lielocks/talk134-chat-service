//package kr.co.talk.global.config.redis;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
//
//@Profile("unittest")
//@Configuration
//@EnableRedisRepositories
//public class TestRedisConfiguration {
//
//    private String redisHost;
//
//    private int redisPort;
//
//
//    public TestRedisConfiguration(@Value("${spring.redis.host}") String redisHost,
//                                  @Value("${spring.redis.port}") int redisPort) {
//        this.redisHost = redisHost;
//        this.redisPort = redisPort;
//    }
//
//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//        RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration();
//        redisConfiguration.setHostName(this.redisHost);
//        redisConfiguration.setPort(this.redisPort);
//        redisConfiguration.setDatabase(0);
//        return new LettuceConnectionFactory(redisConfiguration);
//    }
//
//    @Bean(name = "redisTemplate")
//    public RedisTemplate<?, ?> redisTemplate() {
//        RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(redisConnectionFactory());
//        return redisTemplate;
//    }
//}