package com.flashsale.inventory.lock;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Subsystem Name: Inventory Lock Infrastructure Config
 * Bounded Context: Flash Sale Domain - Redis Connection Management
 * Responsibility: Initializes and exposes the RedissonClient bean configured with connection pooling.
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%d", redisHost, redisPort);

        config.useSingleServer()
                .setAddress(redisAddress)
                .setConnectionMinimumIdleSize(5)
                .setConnectionPoolSize(50)
                .setConnectTimeout(10000)
                .setTimeout(3000);

        return Redisson.create(config);
    }
}