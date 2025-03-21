package com.sii.eucaptcha.service.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Factory for creating the appropriate cache client based on configuration.
 */
@Slf4j
@Component
public class CacheClientFactory {

    @Value("${cache.type:memcached}")
    private String cacheType;

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
        if ("redis".equalsIgnoreCase(cacheType)) {
            log.info("Using Redis cache client");
            cacheClient = redisCacheClient;
        } else {
            log.info("Using Memcached cache client");
            cacheClient = memcachedCacheClient;
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