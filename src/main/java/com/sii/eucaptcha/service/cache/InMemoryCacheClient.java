package com.sii.eucaptcha.service.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * In-memory implementation of the CacheClient interface.
 * This implementation stores data in a ConcurrentHashMap with expiration support.
 */
@Slf4j
@Component
public class InMemoryCacheClient implements CacheClient {

    private static class CacheEntry {
        private final Object value;
        private final long expiryTimeMillis;

        public CacheEntry(Object value, int expirySeconds) {
            this.value = value;
            this.expiryTimeMillis = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expirySeconds);
        }

        public Object getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTimeMillis;
        }
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private boolean initialized = false;

    @Override
    public void set(String key, int expiry, Object value) {
        log.debug("InMemoryCacheClient: Setting key {} with value {} and expiry {}s", key, value, expiry);
        cache.put(key, new CacheEntry(value, expiry));
    }

    @Override
    public Object get(String key) {
        CacheEntry entry = cache.get(key);
        
        if (entry == null) {
            log.debug("InMemoryCacheClient: Key {} not found in cache", key);
            return null;
        }
        
        if (entry.isExpired()) {
            log.debug("InMemoryCacheClient: Key {} has expired", key);
            delete(key);
            return null;
        }
        
        log.debug("InMemoryCacheClient: Retrieved key {} with value {}", key, entry.getValue());
        return entry.getValue();
    }

    @Override
    public void delete(String key) {
        log.debug("InMemoryCacheClient: Deleting key {}", key);
        cache.remove(key);
    }

    @Override
    public void init() {
        log.info("Initializing InMemoryCacheClient");
        
        // Schedule a cleanup task to remove expired entries periodically
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupExpiredEntries();
            } catch (Exception e) {
                log.error("Error occurred during cache cleanup", e);
            }
        }, 60, 60, TimeUnit.SECONDS);
        
        initialized = true;
        log.info("InMemoryCacheClient initialized successfully");
    }

    private void cleanupExpiredEntries() {
        int beforeSize = cache.size();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int removedCount = beforeSize - cache.size();
        
        if (removedCount > 0) {
            log.debug("InMemoryCacheClient: Cleaned up {} expired entries", removedCount);
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Returns the current number of entries in the cache.
     * This method is primarily for testing and monitoring purposes.
     *
     * @return the number of entries in the cache
     */
    public int size() {
        return cache.size();
    }
    
    /**
     * Shuts down the cleanup scheduler when this bean is destroyed.
     */
    public void shutdown() {
        log.info("Shutting down InMemoryCacheClient");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}