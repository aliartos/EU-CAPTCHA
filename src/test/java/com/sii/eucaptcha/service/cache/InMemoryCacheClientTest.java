package com.sii.eucaptcha.service.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class InMemoryCacheClientTest {

    @InjectMocks
    private InMemoryCacheClient cacheClient;

    @AfterEach
    public void cleanup() {
        // Ensure the scheduler is shutdown after tests
        cacheClient.shutdown();
    }

    @Test
    public void testSet() {
        // Set a value in the cache
        cacheClient.set("testKey", 3600, "testValue");
        
        // Verify that the value is in the cache (size check)
        assertEquals(1, cacheClient.size());
        
        // Verify that the value can be retrieved
        assertEquals("testValue", cacheClient.get("testKey"));
    }

    @Test
    public void testGet() {
        // Set a value
        cacheClient.set("testKey", 3600, "testValue");
        
        // Get the value
        Object result = cacheClient.get("testKey");
        
        // Verify the result
        assertEquals("testValue", result);
    }

    @Test
    public void testGetNonExistent() {
        // Try to get a non-existent key
        Object result = cacheClient.get("nonExistentKey");
        
        // Verify that null is returned for a non-existent key
        assertNull(result);
    }

    @Test
    public void testDelete() {
        // Set a value
        cacheClient.set("testKey", 3600, "testValue");
        
        // Verify it exists
        assertEquals("testValue", cacheClient.get("testKey"));
        
        // Delete the value
        cacheClient.delete("testKey");
        
        // Verify it's gone
        assertNull(cacheClient.get("testKey"));
        assertEquals(0, cacheClient.size());
    }

    @Test
    public void testExpiry() throws InterruptedException {
        // Set a value with a very short expiry
        cacheClient.set("shortLived", 1, "This will expire quickly");
        
        // Initially the value should be there
        assertEquals("This will expire quickly", cacheClient.get("shortLived"));
        
        // Wait for expiry
        Thread.sleep(1200); // 1.2 seconds, just to be safe
        
        // Verify the value is gone due to expiry
        assertNull(cacheClient.get("shortLived"));
    }

    @Test
    public void testInit() {
        // Init the cache
        cacheClient.init();
        
        // Verify it's initialized
        assertTrue(cacheClient.isInitialized());
    }

    @Test
    public void testMultipleValues() {
        // Add multiple values
        cacheClient.set("key1", 3600, "value1");
        cacheClient.set("key2", 3600, "value2");
        cacheClient.set("key3", 3600, "value3");
        
        // Verify size
        assertEquals(3, cacheClient.size());
        
        // Verify individual values
        assertEquals("value1", cacheClient.get("key1"));
        assertEquals("value2", cacheClient.get("key2"));
        assertEquals("value3", cacheClient.get("key3"));
        
        // Delete one
        cacheClient.delete("key2");
        
        // Verify size after deletion
        assertEquals(2, cacheClient.size());
        
        // Verify remaining values
        assertEquals("value1", cacheClient.get("key1"));
        assertNull(cacheClient.get("key2"));
        assertEquals("value3", cacheClient.get("key3"));
    }
    
    @Test
    public void testUpdateValue() {
        // Set initial value
        cacheClient.set("updateKey", 3600, "initial");
        assertEquals("initial", cacheClient.get("updateKey"));
        
        // Update the value
        cacheClient.set("updateKey", 1800, "updated");
        
        // Verify the update
        assertEquals("updated", cacheClient.get("updateKey"));
    }
}