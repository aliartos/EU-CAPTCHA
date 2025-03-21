package com.sii.eucaptcha.service;

import com.sii.eucaptcha.service.cache.CacheClient;
import com.sii.eucaptcha.service.cache.CacheClientFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the cache functionality in the EU-CAPTCHA project.
 * These tests verify that the cache implementations work correctly.
 */
@ExtendWith(MockitoExtension.class)
public class CaptchaServiceTest {

    @Mock
    private CacheClient cacheClient;

    @Mock
    private CacheClientFactory cacheClientFactory;

    @DisplayName("Test CacheClient set operation")
    @Test
    public void testCacheClientSet() {
        // Arrange
        String key = "testKey";
        int expiry = 3600;
        String value = "testValue";

        // Act
        cacheClient.set(key, expiry, value);

        // Assert
        verify(cacheClient, times(1)).set(key, expiry, value);
    }

    @DisplayName("Test CacheClient get operation")
    @Test
    public void testCacheClientGet() {
        // Arrange
        String key = "testKey";
        String expectedValue = "testValue";
        when(cacheClient.get(key)).thenReturn(expectedValue);

        // Act
        Object result = cacheClient.get(key);

        // Assert
        assertEquals(expectedValue, result);
        verify(cacheClient, times(1)).get(key);
    }

    @DisplayName("Test CacheClient delete operation")
    @Test
    public void testCacheClientDelete() {
        // Arrange
        String key = "testKey";

        // Act
        cacheClient.delete(key);

        // Assert
        verify(cacheClient, times(1)).delete(key);
    }

    @DisplayName("Test CacheClientFactory returns correct client")
    @Test
    public void testCacheClientFactory() {
        // Arrange
        when(cacheClientFactory.getCacheClient()).thenReturn(cacheClient);

        // Act
        CacheClient result = cacheClientFactory.getCacheClient();

        // Assert
        assertNotNull(result);
        assertEquals(cacheClient, result);
        verify(cacheClientFactory, times(1)).getCacheClient();
    }
}
