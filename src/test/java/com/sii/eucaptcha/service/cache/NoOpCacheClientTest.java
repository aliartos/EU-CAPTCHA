package com.sii.eucaptcha.service.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class NoOpCacheClientTest {

    @InjectMocks
    private NoOpCacheClient cacheClient;

    @Test
    public void testSet() {
        // This is a no-op operation, so we just verify it doesn't throw an exception
        cacheClient.set("testKey", 3600, "testValue");
    }

    @Test
    public void testGet() {
        // The NoOpCacheClient should always return null
        Object result = cacheClient.get("testKey");
        assertNull(result);
    }

    @Test
    public void testDelete() {
        // This is a no-op operation, so we just verify it doesn't throw an exception
        cacheClient.delete("testKey");
    }

    @Test
    public void testInit() {
        // Test that init sets the initialized flag correctly
        cacheClient.init();
        assertTrue(cacheClient.isInitialized());
    }

    @Test
    public void testIsInitialized() {
        // Before init, should be false
        assertFalse(cacheClient.isInitialized());
        
        // After init, should be true
        cacheClient.init();
        assertTrue(cacheClient.isInitialized());
    }
}