package com.sii.eucaptcha.service.cache;

import net.spy.memcached.MemcachedClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemcachedCacheClientTest {

    @Mock
    private MemcachedClient memcachedClient;

    @InjectMocks
    private MemcachedCacheClient cacheClient;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(cacheClient, "memcachedHost", "localhost:11211");
        ReflectionTestUtils.setField(cacheClient, "memcachedHostname", "localhost");
        ReflectionTestUtils.setField(cacheClient, "client", memcachedClient);
    }

    @Test
    public void testSet() {
        // Arrange
        String key = "testKey";
        int expiry = 3600;
        String value = "testValue";
        
        // Act
        cacheClient.set(key, expiry, value);
        
        // Assert
        verify(memcachedClient, times(1)).set(key, expiry, value);
    }

    @Test
    public void testGet() {
        // Arrange
        String key = "testKey";
        String expectedValue = "testValue";
        when(memcachedClient.get(key)).thenReturn(expectedValue);
        
        // Act
        Object result = cacheClient.get(key);
        
        // Assert
        assertEquals(expectedValue, result);
        verify(memcachedClient, times(1)).get(key);
    }

    @Test
    public void testDelete() {
        // Arrange
        String key = "testKey";
        
        // Act
        cacheClient.delete(key);
        
        // Assert
        verify(memcachedClient, times(1)).delete(key);
    }

    @Test
    public void testIsInitialized() {
        // Arrange
        when(memcachedClient.isConfigurationInitialized()).thenReturn(true);
        
        // Act
        boolean result = cacheClient.isInitialized();
        
        // Assert
        assertTrue(result);
        verify(memcachedClient, times(1)).isConfigurationInitialized();
    }

    @Test
    public void testIsInitializedWhenFalse() {
        // Arrange
        when(memcachedClient.isConfigurationInitialized()).thenReturn(false);
        
        // Act
        boolean result = cacheClient.isInitialized();
        
        // Assert
        assertFalse(result);
        verify(memcachedClient, times(1)).isConfigurationInitialized();
    }
}