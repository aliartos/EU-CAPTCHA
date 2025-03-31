package com.sii.eucaptcha.service.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedisCacheClientTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private RedisConnectionFactory connectionFactory;

    @InjectMocks
    private RedisCacheClient cacheClient;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(cacheClient, "redisHost", "localhost");
        ReflectionTestUtils.setField(cacheClient, "redisPort", 6379);
        ReflectionTestUtils.setField(cacheClient, "redisPassword", "");
        ReflectionTestUtils.setField(cacheClient, "redisTemplate", redisTemplate);

        // Use lenient() to avoid UnnecessaryStubbingException
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
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
        verify(valueOperations, times(1)).set(key, value, expiry, TimeUnit.SECONDS);
    }

    @Test
    public void testGet() {
        // Arrange
        String key = "testKey";
        String expectedValue = "testValue";
        when(valueOperations.get(key)).thenReturn(expectedValue);

        // Act
        Object result = cacheClient.get(key);

        // Assert
        assertEquals(expectedValue, result);
        verify(valueOperations, times(1)).get(key);
    }

    @Test
    public void testDelete() {
        // Arrange
        String key = "testKey";

        // Act
        cacheClient.delete(key);

        // Assert
        verify(redisTemplate, times(1)).delete(key);
    }

    @Test
    public void testIsInitialized() {
        // Act
        boolean result = cacheClient.isInitialized();

        // Assert
        assertTrue(result);
    }

    @Test
    public void testIsInitializedWhenNull() {
        // Arrange
        ReflectionTestUtils.setField(cacheClient, "redisTemplate", null);

        // Act
        boolean result = cacheClient.isInitialized();

        // Assert
        assertFalse(result);
    }

    @Test
    public void testIsInitializedWhenConnectionFactoryNull() {
        // Arrange
        when(redisTemplate.getConnectionFactory()).thenReturn(null);

        // Act
        boolean result = cacheClient.isInitialized();

        // Assert
        assertFalse(result);
    }
}
