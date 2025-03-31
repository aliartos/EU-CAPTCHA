package com.sii.eucaptcha.service.cache;

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
public class CacheClientFactoryTest {

    @Mock
    private MemcachedCacheClient memcachedCacheClient;

    @Mock
    private RedisCacheClient redisCacheClient;
    
    @Mock
    private InMemoryCacheClient inMemoryCacheClient;

    @InjectMocks
    private CacheClientFactory cacheClientFactory;

    @BeforeEach
    public void setup() {
        // Use lenient() to avoid UnnecessaryStubbingException
        lenient().when(memcachedCacheClient.isInitialized()).thenReturn(true);
        lenient().when(redisCacheClient.isInitialized()).thenReturn(true);
        lenient().when(inMemoryCacheClient.isInitialized()).thenReturn(true);
    }

    @Test
    public void testInitWithMemcachedAndAwsEnabled() {
        // Arrange
        ReflectionTestUtils.setField(cacheClientFactory, "cacheType", "memcached");
        ReflectionTestUtils.setField(cacheClientFactory, "awsEnabled", true);

        // Act
        cacheClientFactory.init();
        CacheClient result = cacheClientFactory.getCacheClient();

        // Assert
        assertNotNull(result);
        assertEquals(memcachedCacheClient, result);
        verify(memcachedCacheClient, times(1)).init();
        verify(memcachedCacheClient, times(1)).isInitialized();
        verify(redisCacheClient, never()).init();
        verify(inMemoryCacheClient, never()).init();
    }

    @Test
    public void testInitWithMemcachedButAwsDisabled() {
        // Arrange
        ReflectionTestUtils.setField(cacheClientFactory, "cacheType", "memcached");
        ReflectionTestUtils.setField(cacheClientFactory, "awsEnabled", false);

        // Act
        cacheClientFactory.init();
        CacheClient result = cacheClientFactory.getCacheClient();

        // Assert
        assertNotNull(result);
        assertEquals(redisCacheClient, result);
        verify(redisCacheClient, times(1)).init();
        verify(redisCacheClient, times(1)).isInitialized();
        verify(memcachedCacheClient, never()).init();
        verify(inMemoryCacheClient, never()).init();
    }

    @Test
    public void testInitWithRedis() {
        // Arrange
        ReflectionTestUtils.setField(cacheClientFactory, "cacheType", "redis");

        // Act
        cacheClientFactory.init();
        CacheClient result = cacheClientFactory.getCacheClient();

        // Assert
        assertNotNull(result);
        assertEquals(redisCacheClient, result);
        verify(redisCacheClient, times(1)).init();
        verify(redisCacheClient, times(1)).isInitialized();
        verify(memcachedCacheClient, never()).init();
        verify(inMemoryCacheClient, never()).init();
    }
    
    @Test
    public void testInitWithMemory() {
        // Arrange
        ReflectionTestUtils.setField(cacheClientFactory, "cacheType", "memory");

        // Act
        cacheClientFactory.init();
        CacheClient result = cacheClientFactory.getCacheClient();

        // Assert
        assertNotNull(result);
        assertEquals(inMemoryCacheClient, result);
        verify(inMemoryCacheClient, times(1)).init();
        verify(inMemoryCacheClient, times(1)).isInitialized();
        verify(redisCacheClient, never()).init();
        verify(memcachedCacheClient, never()).init();
    }

    @Test
    public void testInitWithUnknownType() {
        // Arrange
        ReflectionTestUtils.setField(cacheClientFactory, "cacheType", "unknown");
        ReflectionTestUtils.setField(cacheClientFactory, "awsEnabled", false); // Default is false

        // Act
        cacheClientFactory.init();
        CacheClient result = cacheClientFactory.getCacheClient();

        // Assert
        assertNotNull(result);
        assertEquals(redisCacheClient, result); // Default to Redis when AWS is disabled
        verify(redisCacheClient, times(1)).init();
        verify(redisCacheClient, times(1)).isInitialized();
        verify(memcachedCacheClient, never()).init();
        verify(inMemoryCacheClient, never()).init();
    }

    @Test
    public void testInitWithUnknownTypeAndAwsEnabled() {
        // Arrange
        ReflectionTestUtils.setField(cacheClientFactory, "cacheType", "unknown");
        ReflectionTestUtils.setField(cacheClientFactory, "awsEnabled", true);

        // Act
        cacheClientFactory.init();
        CacheClient result = cacheClientFactory.getCacheClient();

        // Assert
        assertNotNull(result);
        assertEquals(redisCacheClient, result); // Still use Redis for unknown types
        verify(redisCacheClient, times(1)).init();
        verify(redisCacheClient, times(1)).isInitialized();
        verify(memcachedCacheClient, never()).init();
        verify(inMemoryCacheClient, never()).init();
    }

    @Test
    public void testInitWithMemcachedClientNotInitialized() {
        // Arrange
        ReflectionTestUtils.setField(cacheClientFactory, "cacheType", "memcached");
        ReflectionTestUtils.setField(cacheClientFactory, "awsEnabled", true);
        when(memcachedCacheClient.isInitialized()).thenReturn(false);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            cacheClientFactory.init();
        });

        assertEquals("Failed to initialize cache client", exception.getMessage());
        verify(memcachedCacheClient, times(1)).init();
        verify(memcachedCacheClient, times(1)).isInitialized();
    }

    @Test
    public void testInitWithRedisClientNotInitialized() {
        // Arrange
        ReflectionTestUtils.setField(cacheClientFactory, "cacheType", "redis");
        when(redisCacheClient.isInitialized()).thenReturn(false);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            cacheClientFactory.init();
        });

        assertEquals("Failed to initialize cache client", exception.getMessage());
        verify(redisCacheClient, times(1)).init();
        verify(redisCacheClient, times(1)).isInitialized();
    }
    
    @Test
    public void testInitWithInMemoryClientNotInitialized() {
        // Arrange
        ReflectionTestUtils.setField(cacheClientFactory, "cacheType", "memory");
        when(inMemoryCacheClient.isInitialized()).thenReturn(false);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            cacheClientFactory.init();
        });

        assertEquals("Failed to initialize cache client", exception.getMessage());
        verify(inMemoryCacheClient, times(1)).init();
        verify(inMemoryCacheClient, times(1)).isInitialized();
    }
}
