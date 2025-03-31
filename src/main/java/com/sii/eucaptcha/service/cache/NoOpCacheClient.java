package com.sii.eucaptcha.service.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * No-operation implementation of the CacheClient interface.
 * This implementation doesn't actually cache anything and is used when caching is disabled.
 */
@Slf4j
@Component
public class NoOpCacheClient implements CacheClient {

    private boolean initialized = false;

    @Override
    public void set(String key, int expiry, Object value) {
        log.debug("NoOpCacheClient: Ignoring set operation for key {} with value {}", key, value);
        // No-op - we don't store anything
    }

    @Override
    public Object get(String key) {
        log.debug("NoOpCacheClient: Ignoring get operation for key {}", key);
        // Always return null as if the key doesn't exist
        return null;
    }

    @Override
    public void delete(String key) {
        log.debug("NoOpCacheClient: Ignoring delete operation for key {}", key);
        // No-op - we don't delete anything
    }

    @Override
    public void init() {
        log.info("Initializing NoOpCacheClient - no actual caching will be performed");
        initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }
}