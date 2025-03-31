package com.sii.eucaptcha.service.cache;

/**
 * Interface for cache client implementations.
 * This abstraction allows switching between different cache providers (Memcached, Redis, etc.)
 */
public interface CacheClient {
    
    /**
     * Store a value in the cache with a specified expiration time
     * 
     * @param key the key under which to store the value
     * @param expiry expiration time in seconds
     * @param value the value to store
     */
    void set(String key, int expiry, Object value);
    
    /**
     * Retrieve a value from the cache
     * 
     * @param key the key to look up
     * @return the stored value, or null if not found
     */
    Object get(String key);
    
    /**
     * Delete a value from the cache
     * 
     * @param key the key to delete
     */
    void delete(String key);
    
    /**
     * Initialize the cache client
     */
    void init();
    
    /**
     * Check if the cache client is properly initialized and connected
     * 
     * @return true if the client is initialized and connected, false otherwise
     */
    boolean isInitialized();
}