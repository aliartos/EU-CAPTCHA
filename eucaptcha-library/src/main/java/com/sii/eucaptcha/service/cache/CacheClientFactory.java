package com.sii.eucaptcha.service.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Factory for creating the appropriate cache client based on configuration.
 */
@Slf4j
@Component
public class CacheClientFactory {

    @Value("${cache.type:memcached}")
    private String cacheType;

    @Value("${aws.enabled:false}")
    private boolean awsEnabled;

    private final MemcachedCacheClient memcachedCacheClient;
    private final RedisCacheClient redisCacheClient;

    private CacheClient cacheClient;

    @Autowired
    public CacheClientFactory(MemcachedCacheClient memcachedCacheClient, RedisCacheClient redisCacheClient) {
        this.memcachedCacheClient = memcachedCacheClient;
        this.redisCacheClient = redisCacheClient;
    }

    @PostConstruct
    public void init() {
        if (awsEnabled && "memcached".equalsIgnoreCase(cacheType)) {
            log.info("AWS integration enabled and cache type is memcached. Using Memcached cache client.");
            cacheClient = memcachedCacheClient;
        } else {
            if ("memcached".equalsIgnoreCase(cacheType)) {
                log.info("AWS integration disabled but cache type is memcached. Falling back to Redis cache client.");
            } else {
                log.info("Using Redis cache client");
            }
            cacheClient = redisCacheClient;
        }

        cacheClient.init();

        if (!cacheClient.isInitialized()) {
            log.error("Failed to initialize cache client of type: {}", cacheType);
            throw new RuntimeException("Failed to initialize cache client");
        }
    }

    public CacheClient getCacheClient() {
        return cacheClient;
    }
}
