package com.example.chat.config;

import com.example.chat.util.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class RedisConfig {

    @Value("${redis.host:localhost}")
    private String redisHost;

    @Value("${redis.port:6379}")
    private int redisPort;
    
    @Value("${redis.password:}")
    private String redisPassword;

    @PostConstruct
    public void init() {
        // 初始化 RedisUtil
        RedisUtil.init(redisHost, redisPort, redisPassword);
    }
}
