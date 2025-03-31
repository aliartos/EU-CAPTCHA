package com.sii.eucaptcha.service.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis implementation of the CacheClient interface.
 * Uses Spring Data Redis.
 */
@Slf4j
@Component
public class RedisCacheClient implements CacheClient {

    private RedisTemplate<String, Object> redisTemplate;

    @Value("${cache.redis.host:localhost}")
    private String redisHost;

    @Value("${cache.redis.port:6379}")
    private int redisPort;

    @Value("${cache.redis.password:}")
    private String redisPassword;

    @Override
    public void init() {
        try {
            RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
            configuration.setHostName(redisHost);
            configuration.setPort(redisPort);

            if (redisPassword != null && !redisPassword.isEmpty()) {
                configuration.setPassword(redisPassword);
            }

            JedisConnectionFactory connectionFactory = new JedisConnectionFactory(configuration);
            connectionFactory.afterPropertiesSet();
            connectionFactory.start();

            redisTemplate = new RedisTemplate<>();
            redisTemplate.setConnectionFactory(connectionFactory);
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.afterPropertiesSet();

            log.info("Redis client initialized with host: {}, port: {}", redisHost, redisPort);
        } catch (Exception e) {
            log.error("Failed to initialize Redis client", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void set(String key, int expiry, Object value) {
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, value, expiry, TimeUnit.SECONDS);
            log.debug("Added key {} with value {} in the cache", key, value);
        } else {
            log.error("Redis client is not initialized");
        }
    }

    @Override
    public Object get(String key) {
        if (redisTemplate != null) {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("Retrieved key {} with value {} from the cache", key, value);
            return value;
        } else {
            log.error("Redis client is not initialized");
            return null;
        }
    }

    @Override
    public void delete(String key) {
        if (redisTemplate != null) {
            redisTemplate.delete(key);
            log.debug("Deleted key {} from the cache", key);
        } else {
            log.error("Redis client is not initialized");
        }
    }

    @Override
    public boolean isInitialized() {
        return redisTemplate != null && redisTemplate.getConnectionFactory() != null;
    }
}
