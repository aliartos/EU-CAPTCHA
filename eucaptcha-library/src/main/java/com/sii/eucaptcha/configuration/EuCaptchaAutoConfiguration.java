package com.sii.eucaptcha.configuration;

import com.sii.eucaptcha.configuration.properties.ApplicationConfigProperties;
import com.sii.eucaptcha.configuration.properties.SoundConfigProperties;
import com.sii.eucaptcha.configuration.users.CaptchaUsers;
import com.sii.eucaptcha.service.CaptchaAudioService;
import com.sii.eucaptcha.service.CaptchaService;
import com.sii.eucaptcha.service.cache.CacheClientFactory;
import com.sii.eucaptcha.service.cache.MemcachedCacheClient;
import com.sii.eucaptcha.service.cache.RedisCacheClient;
import com.sii.eucaptcha.service.sliding.CaptchaSlidingQuestionService;
import com.sii.eucaptcha.service.sliding.CaptchaSlidingQuestionServiceImpl;
import com.sii.eucaptcha.service.whatsup.CaptchaWhatsUpImagesService;
import com.sii.eucaptcha.service.whatsup.CaptchaWhatsUpImagesServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

/**
 * Auto-configuration for EU Captcha library.
 * This class automatically configures the beans required for the EU Captcha functionality.
 */
@Configuration
@EnableConfigurationProperties({ApplicationConfigProperties.class, SoundConfigProperties.class})
public class EuCaptchaAutoConfiguration {

    /**
     * Creates a CaptchaWhatsUpImagesService bean if one doesn't exist.
     *
     * @return a new CaptchaWhatsUpImagesService instance
     */
    @Bean
    @ConditionalOnMissingBean
    public CaptchaWhatsUpImagesService captchaWhatsUpImagesService() {
        return new CaptchaWhatsUpImagesServiceImpl();
    }

    /**
     * Creates a CaptchaSlidingQuestionService bean if one doesn't exist.
     *
     * @return a new CaptchaSlidingQuestionService instance
     */
    @Bean
    @ConditionalOnMissingBean
    public CaptchaSlidingQuestionService captchaSlidingQuestionService() {
        return new CaptchaSlidingQuestionServiceImpl();
    }

    /**
     * Creates a CaptchaUsers bean if one doesn't exist.
     *
     * @return a new CaptchaUsers instance
     */
    @Bean
    @ConditionalOnMissingBean
    public CaptchaUsers captchaUsers() {
        return new CaptchaUsers();
    }

    /**
     * Creates a MemcachedCacheClient bean if one doesn't exist.
     *
     * @return a new MemcachedCacheClient instance
     */
    @Bean
    @ConditionalOnMissingBean
    public MemcachedCacheClient memcachedCacheClient() {
        return new MemcachedCacheClient();
    }

    /**
     * Creates a RedisCacheClient bean if one doesn't exist.
     *
     * @return a new RedisCacheClient instance
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisCacheClient redisCacheClient() {
        return new RedisCacheClient();
    }

    /**
     * Creates a CacheClientFactory bean if one doesn't exist.
     *
     * @param memcachedCacheClient the memcached cache client
     * @param redisCacheClient the redis cache client
     * @return a new CacheClientFactory instance
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheClientFactory cacheClientFactory(MemcachedCacheClient memcachedCacheClient, RedisCacheClient redisCacheClient) {
        return new CacheClientFactory(memcachedCacheClient, redisCacheClient);
    }

    /**
     * Creates a CaptchaService bean if one doesn't exist.
     *
     * @param captchaWhatsUpImagesService the captcha whats up images service
     * @param captchaSlidingQuestionService the captcha sliding question service
     * @param soundConfigProperties the sound configuration properties
     * @param resourceLoader the resource loader
     * @param captchaUsers the captcha users
     * @param cacheClientFactory the cache client factory
     * @return a new CaptchaService instance
     */
    @Bean
    @ConditionalOnMissingBean
    public CaptchaService captchaService(
            CaptchaWhatsUpImagesService captchaWhatsUpImagesService,
            CaptchaSlidingQuestionService captchaSlidingQuestionService,
            SoundConfigProperties soundConfigProperties,
            ResourceLoader resourceLoader,
            CaptchaUsers captchaUsers,
            CacheClientFactory cacheClientFactory) {
        return new CaptchaService(
                captchaWhatsUpImagesService,
                captchaSlidingQuestionService,
                soundConfigProperties,
                resourceLoader,
                captchaUsers,
                cacheClientFactory);
    }

    /**
     * Creates a CaptchaAudioService bean if one doesn't exist.
     *
     * @return a new CaptchaAudioService instance
     */
    @Bean
    @ConditionalOnMissingBean
    public CaptchaAudioService captchaAudioService() {
        return CaptchaAudioService.newBuilder().build();
    }
}
