package kr.co.talk.global.config;

import org.springframework.context.annotation.Bean;
import feign.Logger;

public class FeignConfig {
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
